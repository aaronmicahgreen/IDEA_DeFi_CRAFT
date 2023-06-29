package org.stream.local.handler;

public enum DataState {
	DOES_NOT_EXIST,
	PARTIAL,
	EXISTS,
	MODIFIED,
	CORRUPTED,
	INVALID;
}