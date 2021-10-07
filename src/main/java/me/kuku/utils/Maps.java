package me.kuku.utils;

import java.util.HashMap;
import java.util.Map;

public class Maps {

	public static <T,K> Map<T, K> of(T k, K v){
		Map<T, K> map = new HashMap<>();
		map.put(k, v);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2, T k3, K v3){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2, T k3, K v3, T k4, K v4){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2, T k3, K v3, T k4, K v4, T k5, K v5){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2, T k3, K v3, T k4, K v4, T k5, K v5, T k6, K v6){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		map.put(k6, v6);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2, T k3, K v3, T k4, K v4, T k5, K v5, T k6, K v6, T k7, K v7){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		map.put(k6, v6);
		map.put(k7, v7);
		return map;
	}

	public static <T,K> Map<T, K> of(T k1, K v1, T k2, K v2, T k3, K v3, T k4, K v4, T k5, K v5, T k6, K v6, T k7, K v7, T k8, K v8){
		Map<T, K> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		map.put(k6, v6);
		map.put(k7, v7);
		map.put(k8, v8);
		return map;
	}

}
