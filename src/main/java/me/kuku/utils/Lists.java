package me.kuku.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Lists {

	private static final Random random = new Random();

	public static <T> List<T> newArrayList(T...t){
		return new ArrayList<>(Arrays.asList(t));
	}

	public static <T> T random(List<T> list){
		int size = list.size();
		if (size == 0) return null;
		return list.get(random.nextInt(size));
	}
}
