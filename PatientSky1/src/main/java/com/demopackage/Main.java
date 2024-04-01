package com.demopackage;

import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {
	public static void main(String[] args) {
		try {
			// JSON file Parsing 
			JSONParser jsonParser = new JSONParser();

			JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("json\\Danny boy.json"));

			JSONObject jsonObject1 = (JSONObject) jsonParser.parse(new FileReader("json\\Emma Win.json"));

			JSONObject jsonObject2 = (JSONObject) jsonParser.parse(new FileReader("json\\Joanna Hef.json"));

			List<UUID> calendarIds = Arrays.asList(UUID.fromString("48cadf26-975e-11e5-b9c2-c8e0eb18c1e9"),
					UUID.fromString("452dccfc-975e-11e5-bfa5-c8e0eb18c1e9"),
					UUID.fromString("48644c7a-975e-11e5-a090-c8e0eb18c1e9"));
			int duration = 15; // Meeting duration in minutes
			LocalDateTime startTime = LocalDateTime.parse("2019-04-23T00:00:00", DateTimeFormatter.ISO_DATE_TIME);
			LocalDateTime endTime = LocalDateTime.parse("2019-04-24T00:00:00", DateTimeFormatter.ISO_DATE_TIME);

			// Find available meeting times 
			List<LocalDateTime> availableTimes = findAvailableTime(jsonObject, calendarIds, duration, startTime,
					endTime);
			List<LocalDateTime> availableTimes1 = findAvailableTime(jsonObject1, calendarIds, duration, startTime,
					endTime);
			List<LocalDateTime> availableTimes2 = findAvailableTime(jsonObject2, calendarIds, duration, startTime,
					endTime);

			// Output the available meeting times
			System.out.println("Available Meeting Times for Danny boy:");
			for (LocalDateTime time : availableTimes) {
				System.out.println(time.format(DateTimeFormatter.ISO_DATE_TIME));
			}

			System.out.println("Available Meeting Times for Emma Win:");
			for (LocalDateTime time : availableTimes1) {
				System.out.println(time.format(DateTimeFormatter.ISO_DATE_TIME));
			}

			System.out.println("Available Meeting Times for Joanna Hef:");
			for (LocalDateTime time : availableTimes2) {
				System.out.println(time.format(DateTimeFormatter.ISO_DATE_TIME));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<LocalDateTime> findAvailableTime(JSONObject jsonData, List<UUID> calendarIds, int duration,
			LocalDateTime startTime, LocalDateTime endTime) {

		List<LocalDateTime> availableTimes = new ArrayList<LocalDateTime>();

		// Get appointments and timeslots from JSON data
		
		JSONArray appointments = (JSONArray) jsonData.get("appointments");
		JSONArray timeslots = (JSONArray) jsonData.get("timeslots");

		// Filter appointments and timeslots for the specified calendar IDs		
		List<JSONObject> calendarAppointments = filterByCalendarIds(appointments, calendarIds);
		List<JSONObject> calendarTimeslots = filterByCalendarIds(timeslots, calendarIds);

		// Convert appointments and timeslots to LocalDateTime objects
		List<LocalDateTime> appointmentTimes = convertToDateTimeList(calendarAppointments, "start", "end");
		List<LocalDateTime> timeslotTimes = convertToDateTimeList(calendarTimeslots, "start", "end");

		// Generate all possible meeting start times within the specified time interval
		LocalDateTime currentTime = startTime;
		while (currentTime.isBefore(endTime)) {
			availableTimes.add(currentTime);
			currentTime = currentTime.plusMinutes(duration);
		}

		// Remove unavailable times based on appointments and timeslots
		availableTimes.removeAll(appointmentTimes);
		availableTimes.removeAll(timeslotTimes);

		return availableTimes;
	}

	public static List<JSONObject> filterByCalendarIds(JSONArray appointments, List<UUID> calendarIds) {
		if (appointments == null || calendarIds == null) {
			return new ArrayList<JSONObject>(); // Return an empty list if either input is null
		}

		List<JSONObject> filteredList = new ArrayList<JSONObject>();
		for (Object obj : appointments) {
			JSONObject jsonObject = (JSONObject) obj;
			Object calendarIdObj = jsonObject.get("calendar_id");
			if (calendarIdObj != null) {
				String calendarIdString = (String) calendarIdObj;
				UUID calendarId = null;
				try {
					calendarId = UUID.fromString(calendarIdString);
				} catch (IllegalArgumentException e) {
					// Handle invalid UUID format
					e.printStackTrace();
				}
				if (calendarId != null && calendarIds.contains(calendarId)) {
					filteredList.add(jsonObject);
				}
			} else {
				System.out.println("Warning: calendar_id is null for appointment: " + jsonObject);
			}
		}
		return filteredList;
	}

	public static List<LocalDateTime> convertToDateTimeList(List<JSONObject> calendarAppointments, String startKey,
			String endKey) {
		List<LocalDateTime> dateTimeList = new ArrayList<LocalDateTime>();
		for (JSONObject jsonObject : calendarAppointments) {
			LocalDateTime startTime = LocalDateTime.parse((String) jsonObject.get(startKey),
					DateTimeFormatter.ISO_DATE_TIME);
			LocalDateTime endTime = LocalDateTime.parse((String) jsonObject.get(endKey),
					DateTimeFormatter.ISO_DATE_TIME);
			Object slotSizeObject = jsonObject.get("slot_size");
			if (slotSizeObject != null && slotSizeObject instanceof Long) {
				long slotSize = (Long) slotSizeObject;
				LocalDateTime currentTime = startTime;
				while (currentTime.isBefore(endTime)) {
					dateTimeList.add(currentTime);
					currentTime = currentTime.plus(Duration.ofMinutes(slotSize));
				}
			} else {
				System.out.println("Warning: Invalid slot size for appointment: " + jsonObject);
			}
		}
		return dateTimeList;
	}

}
