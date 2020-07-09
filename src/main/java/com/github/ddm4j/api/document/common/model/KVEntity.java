package com.github.ddm4j.api.document.common.model;

public class KVEntity<K, V> {
	private K left;
	private V right;

	public K getLeft() {
		return left;
	}

	public void setLeft(K left) {
		this.left = left;
	}

	public V getRight() {
		return right;
	}

	public void setRight(V Right) {
		this.right = Right;
	}

}
