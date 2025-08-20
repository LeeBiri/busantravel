package busantravel.auth;

import busantravel.model.Member;

public class MemberSession {
	private static Member currentUser;
	
	public static void login(Member m) {
		currentUser = m;
	}

	public static void logout() {
		currentUser = null;
	}

	public static String getId() {
		return currentUser != null ? currentUser.getM_id() : null;
	}

	public static String getName() {
		return currentUser != null ? currentUser.getM_name() : null;
	}

	public static boolean isLoggedIn() {
		return currentUser != null;
	}

	public static Member getCurrentUser() {
		return currentUser;
	}
}
