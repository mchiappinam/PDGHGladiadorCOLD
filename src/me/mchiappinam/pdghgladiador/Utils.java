package me.mchiappinam.pdghgladiador;

import java.util.Calendar;

public class Utils {
	public static int strToCalendar(String dia) {
		if(dia.equalsIgnoreCase("domingo"))
			return Calendar.SUNDAY;
		else if(dia.equalsIgnoreCase("segunda"))
			return Calendar.MONDAY;
		else if(dia.equalsIgnoreCase("terca"))
			return Calendar.TUESDAY;
		else if(dia.equalsIgnoreCase("quarta"))
			return Calendar.WEDNESDAY;
		else if(dia.equalsIgnoreCase("quinta"))
			return Calendar.THURSDAY;
		else if(dia.equalsIgnoreCase("sexta"))
			return Calendar.FRIDAY;
		else if(dia.equalsIgnoreCase("sabado"))
			return Calendar.SATURDAY;
		return Calendar.SATURDAY;
	}
}
