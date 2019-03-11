package com.example.sampleapplication.Listeners;

public interface CallBackListener<S,E> {
    public void success(S success);
    public void error(E error);
}
