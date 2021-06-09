package com.github.ddm4j.api.document.common.model;

public class LMREntity<L, M, R> {
	private L left;
	private M middle;
	private R right;

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public M getMiddle() {
		return middle;
	}

	public void setMiddle(M middle) {
		this.middle = middle;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R Right) {
		this.right = Right;
	}

}
