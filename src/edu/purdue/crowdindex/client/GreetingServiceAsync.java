package edu.purdue.crowdindex.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
    void getTask(String input, AsyncCallback<String> callback)
            throws IllegalArgumentException;
    void returnResult(String input, AsyncCallback<String> callback)
            throws IllegalArgumentException;
    //    void printQueryResults(String input, AsyncCallback<String> callback)
    //            throws IllegalArgumentException;
    void registerUser(String input, AsyncCallback<String> callback)
            throws IllegalArgumentException;
    void reset( String input,AsyncCallback<String> callback)
            throws IllegalArgumentException;
    void resetUserTasks( String input,AsyncCallback<String> callback)
            throws IllegalArgumentException;
    void getAvailTask( String input,AsyncCallback<String> callback)
            throws IllegalArgumentException;
    void signUp( String input,AsyncCallback<String> callback)
            throws IllegalArgumentException;
    void test( String input,AsyncCallback<String> callback)
            throws IllegalArgumentException;
}
