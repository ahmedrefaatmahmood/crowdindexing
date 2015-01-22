package edu.purdue.crowdindex.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
    String getTask(String name) throws IllegalArgumentException;
    String getTaskById(String name) throws IllegalArgumentException;
    String getTaskByTaskGroupId(String name) throws IllegalArgumentException;
    String returnResult(String name) throws IllegalArgumentException;
    String returnResultAMT(String s) throws IllegalArgumentException;
    String cleanUserData(String s) throws IllegalArgumentException;
    //  String printQueryResults(String s) throws IllegalArgumentException;
    String registerUser(String s) throws IllegalArgumentException;
    String signUp(String s) throws IllegalArgumentException;
    String reset(String s) throws IllegalArgumentException;
    String getAvailTask(String s) throws IllegalArgumentException;
    String test(String s) throws IllegalArgumentException;
    String resetUserTasks( String input) throws IllegalArgumentException;
    String skipTask( String input) throws IllegalArgumentException;
}
