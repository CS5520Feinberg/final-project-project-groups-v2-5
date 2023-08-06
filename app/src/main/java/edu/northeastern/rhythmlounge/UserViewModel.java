package edu.northeastern.rhythmlounge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private MutableLiveData<String> usernameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> emailLiveData = new MutableLiveData<>();

    public void setUsername(String username) {
        usernameLiveData.setValue(username);
    }

    public LiveData<String> getUsernameLiveData() {
        return usernameLiveData;
    }

    public void setEmail(String email) {
        emailLiveData.setValue(email);
    }

    public LiveData<String> getEmailLiveData() {
        return emailLiveData;
    }
}

