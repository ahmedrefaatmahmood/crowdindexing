package edu.purdue.crowdindex.client;

import java.util.HashMap;

import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.SliderBar;

import edu.purdue.crowdindex.shared.control.Constants;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Imageindexwebapp implements EntryPoint {
	private static String buttonWidth = "10em";
	private static String longseperator = "40em";
	private static String smallButtonWidth = "3em";
	String taskId;
	String queryId;
	String queryItem;
	String taskType;
	String dataItems;
	String dataset;
	String dataItemsDetails;
	String dataInformedFrequenceies;
	// HorizontalPanel fPanelLeft;
	FlowPanel fPanelLeft;
	HorizontalPanel fPanelMiddle;
	FlowPanel fPanelRight;
	HorizontalPanel center;
	String selectedResult;
	Label textToServerLabel;
	boolean resultSend;
	DialogBox dialogBox;
	DialogBox confirmSubmitBox;

	Button accept;
	Button cancel;
	HTML confirmMessage;

	DialogBox tasksDoneBox;
	Button done;
	Button retake;
	HTML taskDoneMessage;

	Button closeButton;
	HTML serverResponseLabel;
	TextBox nameField;
	PasswordTextBox passwordField;
	Button registerUserButton;
	Button getTaskButton;
	Button resetButton;
	Button signUpButton;
	Button test;
	String userId;
	Label errorLabel;
	Label taskAvailableLable;

	ProgressBar bar;

	Label warninglabel;
	Label instructionslabel;
	Label userNameLabel;
	Label passwordLable;
	int progress;

	/**
	 * 
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	// Get next available task from server
	private void getTask() {
		errorLabel.setText("");
		String textToServer = userId + ";" + taskId + ";" + taskType + ";"
				+ queryId;

		// Then, we send the input to the server.
		// getTaskButton.setEnabled(false);
		textToServerLabel.setText(textToServer);
		serverResponseLabel.setText("");
		if (resultSend) {
			greetingService.getTask(textToServer, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					errorLabel.setText(SERVER_ERROR);
				}

				@Override
				public void onSuccess(String result) {
					if (!result.equals("No  tasks for now")) {
						// got a task
						selectedResult = Constants.Unselected_item;
						resultSend = false;
						String[] s = result.split(";");
						taskId = s[0];
						queryId = s[1];
						queryItem = s[2];
						dataItems = s[3];
						taskType = s[4];
						dataset = s[5];
						// showMessageBox(dataset);
						if ("2".equals(dataset)) {
							dataItemsDetails = s[6];
							// errorLabel.setText(dataItemsDetails);
							// showMessageBox(dataItemsDetails);
						}
						if ("informed".equals(taskType)) {
							dataInformedFrequenceies = s[7];
						}

						// populate task
						addQueryImageAndInstructions();
						addTaskIndexImage();

						// errorLabel.setText(result);
					} else {
						errorLabel.setText(result);
					}
				}
			});
		} else {
			errorLabel
					.setText("Please submit the result of the previous task first");
		}
	}

	private void showPanels() {
		// RootPanel.get("right").setVisible(true);
		// RootPanel.get("left").setVisible(true);
		// RootPanel.get("container").setVisible(true);
		// RootPanel.get("clear").setVisible(true);

		DOM.getElementById("right").getStyle().setDisplay(Display.BLOCK);
		DOM.getElementById("left").getStyle().setDisplay(Display.BLOCK);
		DOM.getElementById("container").getStyle().setDisplay(Display.BLOCK);
		DOM.getElementById("clear").getStyle().setDisplay(Display.BLOCK);

	}

	private void hidePanels() {
		DOM.getElementById("right").getStyle().setDisplay(Display.NONE);
		DOM.getElementById("left").getStyle().setDisplay(Display.NONE);
		DOM.getElementById("container").getStyle().setDisplay(Display.NONE);
		DOM.getElementById("clear").getStyle().setDisplay(Display.NONE);

	}

	private void addTaskIndexImage() {

		HashMap<String, String> frequenceies = new HashMap<String, String>();
		if ("informed".equals(taskType) && dataInformedFrequenceies != null
				&& !"".equals(dataInformedFrequenceies)
				&& dataInformedFrequenceies.contains(",")) {
			// errorLabel.setText(dataInformedFrequenceies);
			String[] itemsandFrequencies = dataInformedFrequenceies.split(",");
			for (String s : itemsandFrequencies) {
				if (s != null && s.contains("@")) {
					String[] valueandFreq = s.split("@");
					frequenceies.put(valueandFreq[0], valueandFreq[1]);
				}
			}
		}
		fPanelLeft.clear();

		FlowPanel answerWrapper = new FlowPanel();
		answerWrapper.getElement().setId("answer-wrapper");
		answerWrapper.getElement().setAttribute("slidevalue", "0");

		FlowPanel wrapper2 = new FlowPanel();
		wrapper2.getElement().setId("wrapper2-234");
		wrapper2.addStyleName("first-answer-item");

		String[] dataiTemslist = dataItems.split(",");

		RadioButton b;
		b = new RadioButton("radioGroup");
		b.setHeight(smallButtonWidth);
		b.setWidth(buttonWidth);
		if (taskType.equals(Constants.astar))
			b.setTitle("" + Constants.LESS_THAN_SUBTREE);
		else
			b.setTitle("" + 0);
		b.addStyleName("my-button");
		b.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				selectedResult = ((RadioButton) event.getSource()).getTitle();
				System.out.println("hi i was clicked and i am button "
						+ ((RadioButton) event.getSource()).getTitle());
				showConfirmMessageBox();
			}
		});

		// wrapper2.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

		wrapper2.add(b);

		Label l;
		if (Constants.SQUARES_DATA_SET_ID.equals(dataset))
			l = new Label("Smaller size.");
		else
			l = new Label("Less expensive.");
		l.setWidth(buttonWidth);

		wrapper2.add(l);

		if ("informed".equals(taskType) && dataInformedFrequenceies != null
				&& !"".equals(dataInformedFrequenceies)
				&& dataInformedFrequenceies.contains(",")) {
			String value = frequenceies.get("" + 0);
			if (value != null && !"0".equals(value) && !"".equals(value)) {
				wrapper2.getElement().getStyle()
						.setBorderWidth(new Double(value), Unit.PX);
			}

		}
		answerWrapper.add(wrapper2);

		// start adding images
		int i = 2;
		if ("astar".equals(taskType)) {
			i = 1;
		}

		// add items
		for (; i < dataiTemslist.length; i++) {

			wrapper2 = new FlowPanel();
			Image image;

			if (dataset.equals("1")) {
				// Square dataset
				b = new RadioButton("radioGroup");
				b.setHeight(smallButtonWidth);
				b.setWidth(buttonWidth);
				b.setTitle("" + (2 * (i - 1) - 1));
				if (i == dataiTemslist.length - 1
						&& taskType.equals(Constants.astar)) {
					b.setTitle("" + Constants.MAX_SUBTREE_KEY);
				}
				b.addStyleName("my-button");
				b.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						selectedResult = ((RadioButton) event.getSource())
								.getTitle();
						System.out.println("hi i was clicked and i am button "
								+ ((RadioButton) event.getSource()).getTitle());
						showConfirmMessageBox();
					}
				});

				image = new Image(
						"http://storage.googleapis.com/crowdindex/data/squareimages/("
								+ dataiTemslist[i] + ").jpg");
				// image = new Image("squareimages/("+dataiTemslist[i]+").jpg");
				image.setHeight(buttonWidth);
				image.setWidth(buttonWidth);
				PushButton button = new PushButton(image);
				button.setTitle("" + (2 * (i - 1) - 1));
				if (i == dataiTemslist.length - 1
						&& taskType.equals(Constants.astar)) {
					button.setTitle("" + Constants.MAX_SUBTREE_KEY);
				}
				System.out.println("test title is " + button.getTitle());
				button.setHeight(buttonWidth);
				button.setWidth(buttonWidth);
				button.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						selectedResult = ((PushButton) event.getSource())
								.getTitle();
						System.out.println("Result selected as button "
								+ ((PushButton) event.getSource()).getTitle());
						showConfirmMessageBox();
					}
				});
				button.addStyleName("my-button");

				wrapper2.add(b);
				wrapper2.add(button);
				if ("informed".equals(taskType)
						&& dataInformedFrequenceies != null
						&& !"".equals(dataInformedFrequenceies)
						&& dataInformedFrequenceies.contains(",")) {
					String value = frequenceies.get("" + button.getTitle());

					if (value != null && !"0".equals(value)
							&& !"".equals(value))
						wrapper2.getElement().getStyle()
								.setBorderWidth(new Double(value), Unit.PX);

				}
				wrapper2.addStyleName("image-answer-item");
				answerWrapper.add(wrapper2);
			} else {
				// Cars dataset
				String[] dataItemsDetailsList = dataItemsDetails.split("@");

				String[] ss = dataItemsDetailsList[i].split("'");
				// showMessageBox("hi"+dataItemsDetails);
				// VerticalPanel hPanel = new VerticalPanel();
				final FlowPanel hPanel = new FlowPanel();
				hPanel.addStyleName("image-answer-item");
				final String itemId = dataiTemslist[i];
				hPanel.getElement().setId(itemId);
				// hPanel.setSpacing(5);

				// errorLabel.setText(dataItemsDetailsList[i-1]);
				l = new Label();
				l.setText("     ");
				Label l2 = new Label();
				l2.setWidth(buttonWidth);
				l2.setText(ss[2]);
				// fPanelLeft.add(l);
				b = new RadioButton("radioGroup", "         ");

				b.setHeight(smallButtonWidth);
				b.setWidth(buttonWidth);
				b.setTitle("" + (2 * (i - 1) - 1));
				if (i == dataiTemslist.length - 1
						&& taskType.equals(Constants.astar)) {
					b.setTitle("" + Constants.MAX_SUBTREE_KEY);
				}
				b.addStyleName("my-button");
				b.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						selectedResult = ((RadioButton) event.getSource())
								.getTitle();
						System.out.println("hi i was clicked and i am button "
								+ ((RadioButton) event.getSource()).getTitle());
						showConfirmMessageBox();
					}
				});
				hPanel.add(new HTML(""));

				hPanel.add(b);

				// hPanel.add(l2);
				// hPanel.setBorderWidth(1);
				
				int imgCount = 0;
				for (int j = 0; j < new Integer(ss[0]) && j < 4; j++) {
					++imgCount;
					
					image = new Image(
							"http://storage.googleapis.com/crowdindex/data/Image_"
									+ dataiTemslist[i] + "/("
									+ dataiTemslist[i] + "_" + j + ").jpg");
					// image = new
					// Image("data/Image_"+dataiTemslist[i]+"/("+dataiTemslist[i]+"_"+j+").jpg");
					image.setHeight(buttonWidth);
					image.setWidth(buttonWidth);
					PushButton b2 = new PushButton(image);
					b2.addStyleName("img-PushButton");
					
					b2.setTitle("" + (2 * (i - 1) - 1));
					if (i == dataiTemslist.length - 1
							&& taskType.equals(Constants.astar)) {
						b2.setTitle("" + Constants.MAX_SUBTREE_KEY);
					}
					b2.setHeight(buttonWidth);
					b2.setWidth(buttonWidth);
					b2.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							// TODO Auto-generated method stub
							selectedResult = ((PushButton) event.getSource())
									.getTitle();
							System.out.println("Result selected as button "
									+ ((PushButton) event.getSource())
											.getTitle());
							showConfirmMessageBox();
						}
					});
					
					if (j > 0) b2.addStyleName("hidden");
					hPanel.add(b2);

				}

				if ("informed".equals(taskType)
						&& dataInformedFrequenceies != null
						&& !"".equals(dataInformedFrequenceies)
						&& dataInformedFrequenceies.contains(",")) {
					String value = frequenceies.get("" + (2 * (i - 1) - 1));
					if (value != null && !"0".equals(value)
							&& !"".equals(value))
						hPanel.getElement().getStyle()
								.setBorderWidth(new Double(value), Unit.PX);

				}
				
				
				
				if (imgCount > 1){
					PushButton moreImgsToggle = new PushButton(" ");
					moreImgsToggle.getElement().setId("tn-toggle-imgs-"+itemId);
					moreImgsToggle.addStyleName("tn-toggle-imgs");
					
					moreImgsToggle.setHTML("<span class=\"fa fa-chevron-down\"></span>");
					moreImgsToggle.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							js_toggleMoreImgs(itemId);
						}
					});
					hPanel.add(moreImgsToggle);
				}
				
				answerWrapper.add(hPanel);

			}
			
			
			
			
			// create a separator or last item
			wrapper2 = new FlowPanel();

			b = new RadioButton("radioGroup");
			// b.setHeight(smallButtonWidth);
			// b.setWidth(buttonWidth);
			b.setTitle("" + (2 * (i - 1)));
			if (i == dataiTemslist.length - 1
					&& taskType.equals(Constants.astar)) {
				b.setTitle("" + Constants.GREATER_THAN_SUBTREE);
			}
			System.out.println("test title is " + b.getTitle());
			b.addStyleName("my-button");
			b.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					// TODO Auto-generated method stub
					selectedResult = ((RadioButton) event.getSource())
							.getTitle();
					System.out.println("Result selected as button "
							+ ((RadioButton) event.getSource()).getTitle());
					showConfirmMessageBox();
				}
			});
			Image im = new Image("title/seperator.jpg");

			// wrapper2.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

			wrapper2.add(b);

			// add separator, otherwise add last item
			if (i != dataiTemslist.length - 1) {

				l = new Label();
				l.setWidth(smallButtonWidth);
				if ("1".equals(dataset))
					im.setHeight(buttonWidth);
				else
					im.setHeight(longseperator);
				im.setWidth(smallButtonWidth);
				// wrapper2.add(l);
				wrapper2.add(im);
				wrapper2.addStyleName("separator-answer-item");
			} else {

				if ("1".equals(dataset))
					l = new Label("Larger size.");
				else
					l = new Label("More Expensive.");
				l.setWidth(buttonWidth);
				wrapper2.add(l);
				wrapper2.addStyleName("last-answer-item");
			}

			if ("informed".equals(taskType) && dataInformedFrequenceies != null
					&& !"".equals(dataInformedFrequenceies)
					&& dataInformedFrequenceies.contains(",")) {
				String value = frequenceies.get("" + (2 * (i - 1)));

				if (value != null && !"0".equals(value) && !"".equals(value))
					wrapper2.getElement().getStyle()
							.setBorderWidth(new Double(value), Unit.PX);

			}
			answerWrapper.add(wrapper2);

		}
		center.clear();

		center.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		FlowPanel answerWrapperView = new FlowPanel();
		answerWrapperView.getElement().setId("answer-wrapper-view");
		answerWrapperView.add(answerWrapper);

		// create arrow buttons
		PushButton scrollLeft = new PushButton(" ");
		scrollLeft.getElement().setId("tn-scroll-left");
		scrollLeft.setHTML("<span class=\"fa fa-chevron-left\"></span>");
		scrollLeft.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				js_slideLeftAnswerContent();
			}
		});

		PushButton scrollRight = new PushButton(" ");
		scrollRight.getElement().setId("tn-scroll-right");
		scrollRight.setHTML("<span class=\"fa fa-chevron-right\"></span>");
		scrollRight.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				js_slideRightAnswerContent();
			}
		});
		fPanelLeft.add(scrollLeft);
		fPanelLeft.add(answerWrapperView);
		fPanelLeft.add(scrollRight);
		center.add(fPanelLeft);
	}

	void displayImagesOfAnItem(String radioButtonTitle, String dataitemKey,
			HashMap<String, String> frequenceies, String dataItemDetails) {

		// VerticalPanel wrapper2 = new VerticalPanel();
		FlowPanel wrapper2 = new FlowPanel();
		wrapper2.setStyleName("image-item");
		RadioButton b;
		Image image;
		if (dataset.equals("1")) {
			b = new RadioButton("radioGroup");
			b.setHeight(smallButtonWidth);
			b.setWidth(buttonWidth);
			b.setTitle("" + radioButtonTitle);
			b.addStyleName("my-button");
			b.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					// TODO Auto-generated method stub
					selectedResult = ((RadioButton) event.getSource())
							.getTitle();
					System.out.println("hi i was clicked and i am button "
							+ ((RadioButton) event.getSource()).getTitle());
					showConfirmMessageBox();
				}
			});

			image = new Image(
					"http://storage.googleapis.com/crowdindex/data/squareimages/("
							+ dataitemKey + ").jpg");
			// image = new Image("squareimages/("+dataitemKey+").jpg");
			image.setHeight(buttonWidth);
			image.setWidth(buttonWidth);
			PushButton button = new PushButton(image);
			button.setTitle("" + radioButtonTitle);
			System.out.println("test title is " + button.getTitle());
			button.setHeight(buttonWidth);
			button.setWidth(buttonWidth);
			button.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					// TODO Auto-generated method stub
					selectedResult = ((PushButton) event.getSource())
							.getTitle();
					System.out.println("Result selected as button "
							+ ((PushButton) event.getSource()).getTitle());
					showConfirmMessageBox();
				}
			});
			button.addStyleName("my-button");
			// wrapper2.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			wrapper2.add(b);
			wrapper2.add(button);
			if ("informed".equals(taskType) && dataInformedFrequenceies != null
					&& !"".equals(dataInformedFrequenceies)
					&& dataInformedFrequenceies.contains(",")) {
				String value = frequenceies.get("" + button.getTitle());
				// if (value != null && !"0".equals(value) && !"".equals(value))
				// wrapper2.setBorderWidth(new Integer(value));

			}
			wrapper2.setStyleName("wrapper2");
			fPanelLeft.add(wrapper2);
		} else {
			String[] ss = dataItemDetails.split("'");
			// showMessageBox("hi"+dataItemsDetails);
			// VerticalPanel hPanel = new VerticalPanel();
			FlowPanel hPanel = new FlowPanel();
			hPanel.setStyleName("hPanel");
			// hPanel.setSpacing(5);

			// errorLabel.setText(dataItemsDetailsList[i-1]);
			Label l = new Label();
			l.setText("     ");
			Label l2 = new Label();
			l2.setWidth(buttonWidth);
			l2.setText(ss[2]);
			// fPanelLeft.add(l);
			b = new RadioButton("radioGroup", "         ");
			b.setHeight(smallButtonWidth);
			b.setWidth(buttonWidth);
			b.setTitle("" + radioButtonTitle);
			b.addStyleName("my-button");
			b.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					// TODO Auto-generated method stub
					selectedResult = ((RadioButton) event.getSource())
							.getTitle();
					System.out.println("hi i was clicked and i am button "
							+ ((RadioButton) event.getSource()).getTitle());
					showConfirmMessageBox();
				}
			});
			hPanel.add(new HTML(""));
			// hPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			hPanel.add(b);

			// hPanel.add(l2);
			// hPanel.setBorderWidth(1);

			for (int j = 0; j < new Integer(ss[0]) && j < 4; j++) {

				image = new Image(
						"http://storage.googleapis.com/crowdindex/data/Image_"
								+ dataitemKey + "/(" + dataitemKey + "_" + j
								+ ").jpg");
				// image = new
				// Image("data/Image_"+dataitemKey+"/("+dataitemKey+"_"+j+").jpg");
				image.setHeight(buttonWidth);
				image.setWidth(buttonWidth);
				PushButton b2 = new PushButton(image);
				b2.setTitle("" + radioButtonTitle);

				b2.setHeight(buttonWidth);
				b2.setWidth(buttonWidth);
				b2.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						selectedResult = ((PushButton) event.getSource())
								.getTitle();
						System.out.println("Result selected as button "
								+ ((PushButton) event.getSource()).getTitle());
						showConfirmMessageBox();
					}
				});
				hPanel.add(b2);

			}

			if ("informed".equals(taskType) && dataInformedFrequenceies != null
					&& !"".equals(dataInformedFrequenceies)
					&& dataInformedFrequenceies.contains(",")) {
				String value = frequenceies.get("" + radioButtonTitle);
				// if (value != null && !"0".equals(value) && !"".equals(value))
				// hPanel.setBorderWidth(new Integer(value));

			}
			fPanelLeft.add(hPanel);

		}

	}

	void showMessageBox(String text) {

		serverResponseLabel.setHTML(new SafeHtmlBuilder().appendEscapedLines(
				text).toSafeHtml());
		dialogBox.center();
		closeButton.setFocus(true);
	}

	void showTasksDoneBox() {

		taskDoneMessage.setText("Would you like to retake the survey?\n\n");
		tasksDoneBox.center();
		done.setFocus(true);
	}

	void showConfirmMessageBox() {

		confirmMessage.setText("Are you sure you selected the corret answer?");
		confirmSubmitBox.center();
		accept.setVisible(true);
		cancel.setVisible(true);
		accept.setFocus(true);
	}

	private void addQueryImageAndInstructions() {

		// fPanelRight = new FlowPanel();
		fPanelRight.clear();
		SimplePanel wrapper = new SimplePanel();

		Image image;
		// errorLabel.setText(dataset);
		Label l;
		String queryText = "";
		if (dataset.equals("1")) {
			// Squares dataset
			l = new HTML(
					new SafeHtmlBuilder()
							.appendEscapedLines(
									"You are given the following image of the square under question:\r\n\r\n")
							.toSafeHtml());
			l.setStyleName("instructionlabel");
			fPanelRight.add(l);
			queryText = "\r\n\r\nThe target of the experiment is to estimate the size of the square. Below, you are given a collection of squares to compare against.\r\n"
					+ "The squares are sorted from left to right in increasing order of their size.\r\n"
					+ "It is required to find the square with the closest matching size to the query square.\r\n"
					+ "If you think that the size of the query square  is almost equal to one of the squares, then press the button on top of that square.\r\n"
					+ "If you think that the size of the query  square  lies in-between two consecutive squares, then press the button that is in-between the two squares.\r\n";

			image = new Image(
					"http://storage.googleapis.com/crowdindex/data/squareimages/("
							+ queryItem + ").jpg");
			// image = new Image("squareimages/("+queryItem+").jpg");
			image.setHeight(buttonWidth);
			image.setWidth(buttonWidth);

			PushButton b = new PushButton(image);
			b.setHeight(buttonWidth);
			b.setWidth(buttonWidth);
			b.addStyleName("my-button");
			b.setTitle(queryItem);

			wrapper.add(b);

			fPanelRight.add(l);
			wrapper.setStyleName("centerplacement");
			fPanelRight.add(wrapper);
			l = new HTML(new SafeHtmlBuilder().appendEscapedLines(queryText)
					.toSafeHtml());
			l.setStyleName("instructionlabel");
			fPanelRight.add(l);
		} else {
			// cars for now
			// TODO: change to else if
			l = new HTML(
					new SafeHtmlBuilder()
							.appendEscapedLines(
									"You are given the following images of the car under question:\r\n\r\n")
							.toSafeHtml());
			l.setStyleName("instructionlabel");
			fPanelRight.add(l);
			queryText = "\r\n\r\nThe target of the experiment is to estimate the price of the car.\r\n"
					+ "Below, you are given a collection of cars to compare against.\r\n"
					+ "The cars are sorted from left to right in increasing order of their price.\r\n"
					+ "It is required to find the car with the closest matching price to the query car.\r\n"
					+ "If you think that the price of the query car is almost equal to one of the cars, then press the button on top of that car.\r\n"
					+ "If you think that the price of the query car lies in-between two consecutive cars, then press the button that is in-between the two cars.\r\n";

			// image = new Image(
			// "http://storage.googleapis.com/crowdindex/data/Image_"
			// + queryItem + "/(" + queryItem + "_0).jpg");
			// image = new
			// Image("data/Image_"+queryItem+"/("+queryItem+"_0).jpg");
			String[] dataItemsDetailsList = dataItemsDetails.split("@");

			String[] ss = dataItemsDetailsList[0].split("'");
			// showMessageBox("hi"+dataItemsDetails);
			FlowPanel wrapHPanel = new FlowPanel();
			wrapHPanel.getElement().setId("query-wrapper");

			FlowPanel hPanelview = new FlowPanel();
			hPanelview.getElement().setId("query-content-view");

			final FlowPanel hPanel = new FlowPanel();
			hPanel.getElement().setId("query-content");
			hPanel.getElement().setAttribute("slidevalue", "0");
			// HorizontalPanel hPanel = new HorizontalPanel();
			// hPanel.setSpacing(5);
			// hPanel.add(new HTML(""));
			// errorLabel.setText(dataItemsDetailsList[i-1]);

			// add query images
			for (int j = 0; j < new Integer(ss[0]) && j < 4; j++) {

				String imgUrl = "http://storage.googleapis.com/crowdindex/data/Image_"
						+ queryItem + "/(" + queryItem + "_" + j + ").jpg";
				PushButton b2 = createImagePushButton(queryItem, buttonWidth,
						buttonWidth, imgUrl);

				hPanel.add(b2);

			}

			// // create arrow buttons
			// PushButton scrollLeft = new PushButton(" ");
			// scrollLeft.getElement().setId("query-scroll-left");
			// scrollLeft.setHTML("<span class=\"fa fa-chevron-left\"></span>");
			// scrollLeft.setWidth("2em");
			// scrollLeft.setHeight(buttonWidth);
			//
			// scrollLeft.addClickHandler(new ClickHandler() {
			//
			// @Override
			// public void onClick(ClickEvent event) {
			// js_slideLeftQueryContent();
			// }
			// });
			//
			// PushButton scrollRight = new PushButton(" ");
			// scrollRight.getElement().setId("query-scroll-right");
			// scrollRight.setHTML("<span class=\"fa fa-chevron-right\"></span>");
			// scrollRight.setWidth("2em");
			// scrollRight.setHeight(buttonWidth);
			//
			// scrollRight.addClickHandler(new ClickHandler() {
			//
			// @Override
			// public void onClick(ClickEvent event) {
			// js_slideRightQueryContent();
			// }
			// });

			hPanelview.add(hPanel);
			// wrapHPanel.add(scrollLeft);
			wrapHPanel.add(hPanelview);
			// wrapHPanel.add(scrollRight);
			// hPanel.setStyleName("centerplacement");
			// hPanel.setBorderWidth(1);

			fPanelRight.add(wrapHPanel);
			l = new HTML(new SafeHtmlBuilder().appendEscapedLines(queryText)
					.toSafeHtml());
			l.setStyleName("instructionlabel");

			fPanelRight.add(l);
			// fPanelRight.add(l2);
			/*
			 * int i =4,j=3; Grid grid = new Grid(i, j);
			 * 
			 * int numRows = grid.getRowCount(); int numColumns =
			 * grid.getColumnCount(); for (int row = 0; row < numRows; row++) {
			 * 
			 * for (int col = 0; col < numColumns; col++) { image = new
			 * Image("data/Image_"+queryItem+"/("+queryItem+"_"+j+").jpg");
			 * image.setHeight(buttonWidth); image.setWidth(buttonWidth);
			 * grid.setWidget(row, col, image);
			 * 
			 * } } fPanelRight.add(grid);
			 */

		}

		System.out.println(dataset);

		// SimplePanel wrapper2 = new SimplePanel();
		FlowPanel wrapper2 = new FlowPanel();
		PushButton b2 = new PushButton("Submit Result..");
		b2.setHeight(smallButtonWidth);
		b2.setWidth(buttonWidth);
		b2.addStyleName("my-button");
		b2.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				sendResultToServer(selectedResult);
				System.out.println("Sending results to the server");
			}
		});
		b2.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				// TODO Auto-generated method stub
				if (event.getNativeKeyCode() == 32) {
					sendResultToServer(selectedResult);
					System.out.println("Sending results to the server");
				}

			}
		});
		wrapper2.add(b2);

		// fPanelRight.add(wrapper2);

		RootPanel.get("right").add(fPanelRight);
	}

	/**
	 * @param b2
	 */
	private PushButton createImagePushButton(String queryItem,
			String buttonWidth, String buttonHeight, String imgUrl) {

		// image = new
		// Image("data/Image_"+queryItem+"/("+queryItem+"_"+j+").jpg");

		Image image = new Image(imgUrl);
		image.setHeight(buttonWidth);
		image.setWidth(buttonWidth);

		PushButton b2 = new PushButton(image);

		b2.addStyleName("query-image");
		b2.setTitle("" + queryItem);

		b2.setHeight(buttonWidth);
		b2.setWidth(buttonWidth);
		b2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				// selectedResult = ( (PushButton)
				// event.getSource()).getTitle();
				// System.out.println("Result selected as button " +(
				// (PushButton) event.getSource()).getTitle());
			}
		});
		return b2;
	}

	private void sendResultToServer(String result) {
		// First, we validate the input.
		if (selectedResult.equals(Constants.Unselected_item)) {
			errorLabel.setText("Please select an answer first");
		} else if (resultSend) {
			errorLabel
					.setText("Result already send to server please get another task");
		} else if (!resultSend) {
			String toSend = taskId + ";" + userId + ";" + result + ";"
					+ taskType;
			greetingService.returnResult(toSend, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {

					errorLabel.setText("Error in sending result to server");
				}

				@Override
				public void onSuccess(String result) {
					resultSend = true;
					getTaskButton.setEnabled(true);
					errorLabel.setText("Result send successfully");
					fPanelLeft.clear();
					fPanelRight.clear();
					bar.setProgress(++progress);
					if (bar.getProgress() < Constants.MaxUserTasks) {
						// a user has not finished his survey
						// we can get more tasks
						getTask();
					} else {
						showTasksDoneBox();
						// a user has finished his survey
						// we can get more tasks
						// show a message to tell him that and if he wants to
						// re-take the survey reinitialize
					}

				}
			});
		}
	}

	public static native void js_slideLeftQueryContent() /*-{
		$wnd.slideLeftQueryContent();
	}-*/;

	public static native void js_slideRightQueryContent() /*-{
		$wnd.slideRightQueryContent();
	}-*/;

	public static native void js_slideLeftAnswerContent() /*-{
		$wnd.slideLeftAnswerContent();
	}-*/;

	public static native void js_slideRightAnswerContent() /*-{
		$wnd.slideRightAnswerContent();
	}-*/;
	
	public static native void js_toggleMoreImgs(String container) /*-{
	$wnd.toggleMoreImgs(container);
	}-*/;

	/**
	 * This is the entry point method.
	 */

	@Override
	public void onModuleLoad() {
		// Create a CellTable.
		// errorLabel.setText("test");

		// addVerticalFlowPanelleft() ;
		// addVerticalFlowPanelRight();
		progress = 0;
		bar = new ProgressBar(0, Constants.MaxUserTasks, 0);

		FlowPanel p = new FlowPanel();
		p.add(bar);
		RootPanel.get("statusDiv").add(p);
		bar.setVisible(false);
		bar.setWidth("50em");

		taskId = " ";
		queryId = " ";
		queryItem = " ";
		taskType = " ";

		dialogBox = new DialogBox();
		dialogBox.setText("test code status");

		resultSend = true;
		selectedResult = "-1";

		// fPanelLeft = new HorizontalPanel();// new FlowPanel();
		// fPanelLeft.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		fPanelLeft = new FlowPanel();

		fPanelMiddle = new HorizontalPanel();
		fPanelMiddle
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		RootPanel.get("middleslider").add(fPanelMiddle);

		// remove Slider for now

		// SliderBar slider = new SliderBar(0.0, 100.0);
		// slider.setWidth("50em");
		// slider.setStepSize(1.0);
		// slider.setCurrentValue(50.0);
		// slider.setNumTicks(10);
		// slider.setNumLabels(0);
		//
		// SimplePanel wrapper = new SimplePanel();
		// wrapper.setStyleName("centerplacement");
		// wrapper.add(slider);
		// fPanelMiddle.add(wrapper);

		fPanelRight = new FlowPanel();
		center = new HorizontalPanel();
		center.getElement().setId("centerPanel");

		RootPanel.get("left").add(center);
		registerUserButton = new Button("Log in");
		getTaskButton = new Button("Get task");
		nameField = new TextBox();
		nameField.setText("");
		passwordField = new PasswordTextBox();
		passwordField.setVisible(true);
		passwordField.setText("");
		passwordField.setWidth("10em");
		resetButton = new Button();
		signUpButton = new Button();
		test = new Button("test");

		errorLabel = new Label();

		taskAvailableLable = new Label();

		resetButton.setVisible(false);
		resetButton.setText("Rest");
		signUpButton.setText("Sign up");

		getTaskButton.addStyleName("getTaskButton");
		getTaskButton.setEnabled(false);
		getTaskButton.setVisible(false);

		warninglabel = new Label(
				"Please DO NOT USE Your Purdue Account information.");
		warninglabel.setVisible(true);
		warninglabel.setStyleName("warningLabel");

		instructionslabel = new Label("Please sign up or log in.");
		instructionslabel.setVisible(true);
		instructionslabel.setStyleName("instLabel");

		userNameLabel = new Label("User name");
		userNameLabel.setVisible(true);
		passwordLable = new Label("Password");
		passwordLable.setVisible(true);
		// Add the nameField and getTaskButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("errorLabelContainer").add(errorLabel);
		RootPanel.get("taskAvailabilityLabelContainer").add(taskAvailableLable);

		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("passwordFieldContainer").add(passwordField);
		RootPanel.get("registerUserContainer").add(registerUserButton);
		RootPanel.get("getTaskButtonContainer").add(getTaskButton);
		RootPanel.get("resetButtonContainer").add(resetButton);
		// RootPanel.get("resetButtonContainer").add(bar1);
		RootPanel.get("signUpButtonContainer").add(signUpButton);

		RootPanel.get("signinInstContanier").add(instructionslabel);
		RootPanel.get("warningInstContanier").add(warninglabel);
		RootPanel.get("userLabelContanier").add(userNameLabel);
		RootPanel.get("passwordLabelContanier").add(passwordLable);
		// RootPanel.get("signUpButtonContainer").add(test);

		// hidePanels();

		addTimerToRefreshState();
		addSignUpHnadler();
		addTestButtonAndService();
		addResetButtonHandler();
		addRegisterUserHandler();
		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		buildConcentInfo();
		buildConfirmMessageSubmissionBox();
		buildTaskDoneMessageBox();

	}

	void addTimerToRefreshState() {
		// This may be no longer needed
		Timer t = new Timer() {
			@Override
			public void run() {
				if (!(userId == null || "".equals(userId))) {
					greetingService.getAvailTask(userId,
							new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onSuccess(String result) {
									String[] s = result.split(",");
									taskAvailableLable.setText(s[0]
											+ " tasks avaiable");
								}

							});
				}
			}
		};
		t.scheduleRepeating(3000);
	}

	void addSignUpHnadler() {
		signUpButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (!("".equals(nameField.getText()) || "".equals(passwordField
						.getText()))) {
					greetingService.signUp(nameField.getText() + ";"
							+ passwordField.getText(),
							new AsyncCallback<String>() {

								@Override
								public void onSuccess(String result) {

									errorLabel.setText(result);

								}

								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub

								}
							});
				} else {
					errorLabel.setText("Please enter user name and password");
				}

			}
		});
	}

	void addRegisterUserHandler() {
		registerUserButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// errorLabel.setText("login clicked");
				if (!("".equals(nameField.getText()) || "".equals(passwordField
						.getText()))) {
					greetingService.registerUser(nameField.getText() + ";"
							+ passwordField.getText(),
							new AsyncCallback<String>() {
								@Override
								public void onFailure(Throwable caught) {
									errorLabel.setText(SERVER_ERROR);
								}

								@Override
								public void onSuccess(String result) {
									if (result.contains("Login successfull")) {
										// showPanels();
										String[] s = result.split(":");
										getTaskButton.setEnabled(true);
										getTaskButton.setVisible(true);
										registerUserButton.setVisible(false);
										nameField.setVisible(false);
										passwordField.setVisible(false);
										userId = s[1];
										errorLabel.setText(result);
										if ("1".equals(userId)) {
											resetButton.setVisible(true);
										} else {
											resetButton.setVisible(false);
										}
										signUpButton.setVisible(false);
										passwordLable.setVisible(false);
										userNameLabel.setVisible(false);
										instructionslabel.setVisible(false);
										warninglabel.setVisible(false);
										bar.setVisible(true);

										greetingService.getAvailTask(userId,
												new AsyncCallback<String>() {

													@Override
													public void onFailure(
															Throwable caught) {
														// TODO Auto-generated
														// method stub

													}

													@Override
													public void onSuccess(
															String result) {
														String[] s = result
																.split(",");
														taskAvailableLable
																.setText(s[0]
																		+ " tasks avaiable");
														// bar1.setText(result+" tasks avaiable");
														bar.setProgress(Integer
																.parseInt(s[1]));
														progress = Integer
																.parseInt(s[1]);
														if (progress >= Constants.MaxUserTasks) {
															showTasksDoneBox();
														}

													}

												});

									} else {
										getTaskButton.setEnabled(false);
										getTaskButton.setVisible(false);
										registerUserButton.setVisible(true);
										nameField.setVisible(true);
										passwordField.setVisible(true);
										errorLabel.setText(result);
										signUpButton.setVisible(true);
										passwordLable.setVisible(true);
										userNameLabel.setVisible(true);
										instructionslabel.setVisible(true);
										warninglabel.setVisible(true);
										bar.setVisible(false);
									}
								}
							});

				} else {
					errorLabel.setText("Please enter user name and password");
				}
			}

		});
	}

	void addResetButtonHandler() {
		resetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				greetingService.reset("", new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}
				});

			}
		});

	}

	void addTestButtonAndService() {
		test.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				greetingService.test("", new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						errorLabel.setText(result);

					}

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}
				});

			}
		});

	}

	void buildConcentInfo() {
		// Create the popup dialog box
		dialogBox = new DialogBox();
		dialogBox.setText("Consent infromation");
		dialogBox.setAnimationEnabled(true);
		closeButton = new Button("Accept");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		textToServerLabel = new Label();
		serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		// dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		// dialogVPanel.add(textToServerLabel);
		// dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				// getTaskButton.setEnabled(true);
				// getTaskButton.setFocus(true);
			}
		});

		// Create a handler for the getTaskButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the getTaskButton.
			 */
			@Override
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendNameToServer() {

				getTask();

			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		getTaskButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);

		/* Preparing message boxes */
		showMessageBox("1) This website and tasks shown are for research purposes. "
				+ "The purpose of this research is to study how humans can make decisions in a crowd-sourced environment. "
				+ "The experiment aims at building and querying a tree-like index.\r\n\r\n"
				+ "2) No personal information will be stored in the process.\r\n\r\n"
				+ "3) No risks are involved in the process.\r\n\r\n"
				+ "4) It would take about 5 minutes to finish the survey.\r\n\r\n"
				+ "5) Participation is voluntary. Refusal to participate will not incur any penalty. "
				+ "The user can stop and quit the experiment at any point in time.\r\n\r\n"
				+ "6) Please contact Ahmed Mahmood (amahamoo@purdue.edu) for any questions.\r\n\r\n");
	}

	void buildConfirmMessageSubmissionBox() {
		confirmSubmitBox = new DialogBox();
		confirmSubmitBox.setText("Confirm result submission.");
		confirmSubmitBox.setAnimationEnabled(true);
		accept = new Button("Ok");
		accept.setWidth("8em");
		cancel = new Button("Cancel");
		cancel.setWidth("8em");
		confirmMessage = new HTML();
		VerticalPanel confirmVPanel = new VerticalPanel();
		confirmVPanel.addStyleName("dialogVPanel");

		confirmVPanel.add(confirmMessage);
		confirmVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		HorizontalPanel hPanel2 = new HorizontalPanel();
		hPanel2.add(accept);
		hPanel2.add(cancel);
		confirmVPanel.add(hPanel2);
		confirmSubmitBox.setWidget(confirmVPanel);
		// Add a handler to close the DialogBox
		accept.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				confirmSubmitBox.hide();
				getTaskButton.setEnabled(true);
				getTaskButton.setFocus(true);
				sendResultToServer(selectedResult);
			}
		});
		cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				confirmSubmitBox.hide();
				getTaskButton.setEnabled(true);
				getTaskButton.setFocus(true);
			}
		});
	}

	void buildTaskDoneMessageBox() {

		tasksDoneBox = new DialogBox();
		tasksDoneBox.setText("Survery done.");
		tasksDoneBox.setAnimationEnabled(true);
		done = new Button("No");
		done.setWidth("8em");
		retake = new Button("Yes");
		retake.setWidth("8em");
		taskDoneMessage = new HTML();
		VerticalPanel tasksDoneVPanel = new VerticalPanel();
		tasksDoneVPanel.addStyleName("dialogVPanel");

		tasksDoneVPanel.add(taskDoneMessage);
		tasksDoneVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		HorizontalPanel hPanel3 = new HorizontalPanel();
		hPanel3.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		hPanel3.add(done);
		hPanel3.add(retake);
		tasksDoneVPanel.add(hPanel3);
		tasksDoneBox.setWidget(tasksDoneVPanel);

		// Add a handler to close the DialogBox
		done.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// DO NOTHING
				tasksDoneBox.hide();
				getTaskButton.setEnabled(false);
			}
		});
		retake.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO
				/*
				 * Reset the tasks Reinitiate tasks for the worker
				 */
				tasksDoneBox.hide();

			}
		});

	}

}
