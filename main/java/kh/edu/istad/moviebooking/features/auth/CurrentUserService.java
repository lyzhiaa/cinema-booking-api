package kh.edu.istad.moviebooking.features.auth;

import kh.edu.istad.moviebooking.domain.User;

public interface CurrentUserService {

    User getCurrentUser();
}