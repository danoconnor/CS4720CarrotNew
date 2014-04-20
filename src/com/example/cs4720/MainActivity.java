package com.example.cs4720;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int LOGIN_STATE = 0;
	private static final int MAIN_STATE = 1;
	private static final int ADD_CLASS_STATE = 2;
	private static final int REQ_VIEW_STATE = 3;
	private static final int VIEW_CLASSES_TAKEN_STATE = 4;
	private static final int RATING_FORM_STATE = 5;
	private static final int CREATE_USER_STATE = 6;
	private static final int VIEW_RATING_STATE = 7;
	private static final int PLAN_STATE = 8;
	private static final int PICK_RATING_STATE = 9;
	private static final int WANT_STATE = 10;

	private String username;
	private int projectId;
	private int state;
	private boolean isDefaultDeptSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeLoginPage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void initializeRequirementsViewPage() {
		final Button addClassesButton = (Button) findViewById(R.id.addButton);
		addClassesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.add_class_form);
				initializeAddClassForm("", "");
			}
		});

		final Button planButton = (Button) findViewById(R.id.planButton);
		planButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.classes_that_you_want);
				initializePlanSemesterPage("", "");
			}
		});

		final Button rateButton = (Button) findViewById(R.id.rateButton);
		rateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.pick_class_for_rating);
				initializeAddRatingPage(username, null, null);	
			}
		});

		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				String rowString = ((Button) view).getText().toString();
				String[] rowStringParts = rowString.split("\\s+");
				String reqName = rowStringParts[0]; // first word should be req
													// name

				setContentView(R.layout.fulfillreq);
				initializeRequirementView(reqName);
			}
		};

		try {
			JSONObject result = new WebServiceTask().execute(
					"http://plato.cs.virginia.edu/~cs4720s14carrot/user/"
							+ username).get();
			ArrayList<String> listValues = new ArrayList<String>();
			ArrayList<Button> buttons = new ArrayList<Button>();

			if (result != null) {
				Iterator<String> it = (Iterator<String>) (result.keys());
				int numClasses = 0;
				while (it.hasNext()) {
					String name = (it.next());
					int value = (Integer) (result.get(name));
					numClasses += value;

					String rowString = name + " (" + value + ")";

					listValues.add(rowString);
				}

				Collections.sort(listValues);

				final TextView header = (TextView) findViewById(R.id.header);
				header.setText("You have " + numClasses
						+ " classes left to take!");

				GridView gView = (GridView) findViewById(R.id.reqsGridView);
				gView.setAdapter(new ButtonAdapter(this, listValues, listener));
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		state = MAIN_STATE;
	}

	public void initializeRequirementView(String requirement) {
		final TextView header = (TextView) findViewById(R.id.header);
		header.setText("The following classes fill the "
				+ requirement
				+ " requirement. Choose one to see information about that class:");

		// call web service and get list of results that meet the requirement
		// for each result, add a row to the LinearLayout with the class name,
		// button to add class, button to view class ratings
		final Spinner spin = (Spinner) findViewById(R.id.fulfillReqSpinner);
		try {
			JSONObject result = new WebServiceTask().execute(
					"http://plato.cs.virginia.edu/~cs4720s14carrot/getclasses/"
							+ requirement + "/" + username).get();
			
			if (result != null) {
				Iterator<String> it = (Iterator<String>) (result.keys());

				ArrayList<String> data = new ArrayList<String>();

				while (it.hasNext()) {
					String className = it.next();

					data.add(className);
				}

				Collections.sort(data);

				
				ArrayAdapter<String> reqAdapter = new ArrayAdapter<String>(
						this, android.R.layout.simple_spinner_item, data);
				reqAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spin.setAdapter(reqAdapter);
				spin.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						String selectedClass = (String)(spin.getSelectedItem());
						String dept = selectedClass.substring(0, selectedClass.length() - 4);
						String courseNum = selectedClass.substring(selectedClass.length() - 4);
						
						initializeViewRatingPage(dept, courseNum);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						
					}
				
				});
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final Button addClassesTakenButton = (Button) findViewById(R.id.addTakenClassButton);
		addClassesTakenButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String classSelected = (String) spin.getSelectedItem();
				String dept = classSelected.substring(0, classSelected.length() - 4);
				String courseNum = classSelected.substring(classSelected.length() - 4);
				
				setContentView(R.layout.add_class_form);
				initializeAddClassForm(dept, courseNum);
			}
		});
		
		
		final Button addPlanClassesButton = (Button) findViewById(R.id.addPlanClassButton);
		addPlanClassesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String classSelected = (String) spin.getSelectedItem();
				String dept = classSelected.substring(0, classSelected.length() - 4);
				String courseNum = classSelected.substring(classSelected.length() - 4);
				
				setContentView(R.layout.classes_that_you_want);
				initializePlanSemesterPage(dept, courseNum);
			}
		});
		
		state = REQ_VIEW_STATE;
	}

	private void initializeViewClassesPage() {
		// call web service and get list of classes taken
		// for each result, add a row to the LinearLayout with the class name
		// and a button to add/update a class rating

		final Button addClassesButton = (Button) findViewById(R.id.addButton);
		addClassesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.add_class_form);
				initializeAddClassForm("", "");
			}
		});

		final Button planButton = (Button) findViewById(R.id.planButton);
		planButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.classes_that_you_want);
				initializePlanSemesterPage("", "");
			}
		});

		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String className = ((Button) view).getText().toString();
				String dept = className.substring(0, className.length() - 4);
				String courseNum = className.substring(className.length() - 4);
				
				setContentView(R.layout.pick_class_for_rating);
				initializeAddRatingPage(username, dept, courseNum);
			}
		};

		try {
			JSONObject result = new WebServiceTask().execute(
					"http://plato.cs.virginia.edu/~cs4720s14carrot/taken/"
							+ username).get();

			if (result != null) {
				ArrayList<String> alreadyRated = new ArrayList<String>();
				JSONObject alreadyRatedResult = new WebServiceTask().execute(
						"http://plato.cs.virginia.edu/~cs4720s14carrot/alreadyrated/"
								+ username).get();

				if (alreadyRatedResult != null) {
					Iterator<String> alreadyIt = (Iterator<String>) (alreadyRatedResult
							.keys());

					while (alreadyIt.hasNext()) {
						alreadyRated.add(alreadyIt.next());
					}
				}

				Iterator<String> it = (Iterator<String>) (result.keys());
				ArrayList<String> data = new ArrayList<String>();
				while (it.hasNext()) {
					String className = it.next();
					data.add(className);
				}

				ButtonAdapter adapter = new ButtonAdapter(this, data, listener);
				GridView gView = (GridView) findViewById(R.id.reqsGridView);
				gView.setAdapter(adapter);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		state = VIEW_CLASSES_TAKEN_STATE;
	}

	private void initializeAddClassForm(String dept, String c) {
		final Button addClassButton = (Button) findViewById(R.id.addClassButton);
		final EditText departmentField = (EditText) findViewById(R.id.departmentField);
		departmentField.setText(dept);
		final EditText courseNumField = (EditText) findViewById(R.id.courseNumField);
		courseNumField.setText(c);
		final ArrayList<String> data = new ArrayList<String>();
		
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String className = ((Button) view).getText().toString();
				String dept = className.substring(0, className.length() - 4);
				String courseNum = className.substring(className.length() - 4);
				
				setContentView(R.layout.pick_class_for_rating);
				initializeAddRatingPage(username, dept, courseNum);
			}
		};
		
		final ButtonAdapter adapter = new ButtonAdapter(this, data, listener);
		final GridView gView = (GridView) findViewById(R.id.reqsGridView);

		addClassButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String department = departmentField.getText().toString()
						.toUpperCase();
				String courseNum = courseNumField.getText().toString();

				String url = "http://plato.cs.virginia.edu/~cs4720s14carrot/update/"
						+ username;

				try {
					JSONObject result = new WebServiceTask().execute(url,
							"department", department, "courseNum", courseNum)
							.get();
					String isSuccess = (String) (result.get("success"));
					
					if (isSuccess.equals("true")) {
						adapter.add(department + courseNum);
						gView.setAdapter(adapter);
						
					} else {
						departmentField.setText("");
						courseNumField.setText("");

						Toast toast = Toast.makeText(v.getContext(),
								"Class not found, please try again",
								Toast.LENGTH_SHORT);
						toast.show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(departmentField.getWindowToken(), 0);
				mgr.hideSoftInputFromWindow(courseNumField.getWindowToken(), 0);
			}
		});
		
		try {
			JSONObject result = new WebServiceTask().execute(
					"http://plato.cs.virginia.edu/~cs4720s14carrot/taken/"
							+ username).get();

			if (result != null) {
				ArrayList<String> alreadyRated = new ArrayList<String>();
				JSONObject alreadyRatedResult = new WebServiceTask().execute(
						"http://plato.cs.virginia.edu/~cs4720s14carrot/alreadyrated/"
								+ username).get();

				if (alreadyRatedResult != null) {
					Iterator<String> alreadyIt = (Iterator<String>) (alreadyRatedResult
							.keys());

					while (alreadyIt.hasNext()) {
						alreadyRated.add(alreadyIt.next());
					}
				}

				Iterator<String> it = (Iterator<String>) (result.keys());
				while (it.hasNext()) {
					String className = it.next();
					data.add(className);
				}

				gView.setAdapter(adapter);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		state = ADD_CLASS_STATE;
	}

	private void initializeLoginPage() {
		final Button loginButton = (Button) findViewById(R.id.loginButton);
		final Button createUserButton = (Button) findViewById(R.id.createUserButton);
		final EditText userNameField = (EditText) findViewById(R.id.usernameLoginField);

		final TextView userNameTV = (TextView) findViewById(R.id.usernameTV);
//		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
//				RelativeLayout.LayoutParams.WRAP_CONTENT,
//				RelativeLayout.LayoutParams.WRAP_CONTENT);
//		Display display = getWindowManager().getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		int marginTop = (int)(Math.round((double)(size.y) / 4.0));
//		layout.setMargins(0, marginTop, 0, 0);
//		userNameTV.setLayoutParams(layout);
		
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(userNameField.getWindowToken(), 0);

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				username = userNameField.getText().toString();

				if (username.length() > 0) {
					String checkUserUrl = "http://plato.cs.virginia.edu/~cs4720s14carrot/check/"
							+ username;
					try {
						JSONObject result = new WebServiceTask().execute(
								checkUserUrl).get();
						if (result != null) {
							String userExists = result.getString("userexists");

							if (userExists.equals("true")) {
								
								String projectIdURL = "http://plato.cs.virginia.edu/~cs4720s14carrot/getProjectUser/" + username;
								JSONObject projectIdResult = new WebServiceTask().execute(projectIdURL).get();
								
								projectId = projectIdResult.getInt("project");
								Log.i("PROJECT ID FOR " + username, projectId + "");
								setContentView(R.layout.requirements_page);
								initializeRequirementsViewPage();
							} else {
								userNameField.setText("");

								Toast toast = Toast.makeText(arg0.getContext(),
										"User '" + username
												+ "' does not exist",
										Toast.LENGTH_SHORT);
								toast.show();
							}
						} else {
							Toast toast = Toast.makeText(
									arg0.getContext(),
									"Problem communicating with the login service\nPlease try again later",
									Toast.LENGTH_SHORT);
							toast.show();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(userNameField.getWindowToken(), 0);
			}
		});

		createUserButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.create_user);
				initializeCreateUserPage();
			}
		});

		state = LOGIN_STATE;
	}

	private void initializeViewRatingPage(String dept, String courseNum) {
		final TextView header = (TextView) findViewById(R.id.viewRatingHeader);
		header.setText("Ratings for " + dept + courseNum);

		final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.viewRatingLinearLayout);
		linearLayout.removeAllViews();

		try {
			String url = "http://plato.cs.virginia.edu/~cs4720s14carrot/getAvgRatings";
			JSONObject result = new WebServiceTask().execute(url, "deptID", dept, "courseNum", courseNum).get();

			if (result != null) {
				for (int i = 0; i < result.names().length(); i++) {
					Log.i("RATING", result.names().getString(i));
				}

				Iterator<String> it = (Iterator<String>) (result.keys());

				while (it.hasNext()) {
					String profId = it.next();
					RelativeLayout row = new RelativeLayout(this);

					RelativeLayout.LayoutParams classLayout = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					classLayout.addRule(RelativeLayout.CENTER_VERTICAL);
					TextView profTV = new TextView(this);
					profTV.setText(profId);
					profTV.setTextSize(18);
					profTV.setLayoutParams(classLayout);
					row.addView(profTV);
					linearLayout.addView(row);

					if (result.getString(profId).length() > 0) {
						ArrayList<String> ratingsList = new ArrayList<String>();

						String value = result.getString(profId);
						String currentRating = "";
						for (int i = 0; i < value.length(); i++) {
							if (Character.isDigit(value.charAt(i))
									|| value.charAt(i) == '.') {
								currentRating += value.charAt(i);
							} else {
								
								double ratingDouble = Double.parseDouble(currentRating);
								ratingDouble = Math.round(ratingDouble * 100) / 100.0;
								ratingsList.add(ratingDouble + "");
								currentRating = new String();
							}
						}

						if (currentRating.length() > 0) {
							double ratingDouble = Double.parseDouble(currentRating);
							ratingDouble = Math.round(ratingDouble * 100) / 100.0;
							ratingsList.add(ratingDouble + "");
						}

						RelativeLayout.LayoutParams rowLayout = new RelativeLayout.LayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						TextView instrRating = new TextView(this);
						instrRating.setText("Instructor Rating: "
								+ ratingsList.get(0));
						instrRating.setLayoutParams(rowLayout);
						linearLayout.addView(instrRating);

						TextView diffRating = new TextView(this);
						diffRating
								.setText("Difficulty Rating (1=easy, 5 = hard): "
										+ ratingsList.get(1));
						diffRating.setLayoutParams(rowLayout);
						linearLayout.addView(diffRating);

						TextView timeRating = new TextView(this);
						timeRating
								.setText("Time Commitment (1= little time, 5=all your time): "
										+ ratingsList.get(2));
						timeRating.setLayoutParams(rowLayout);
						linearLayout.addView(timeRating);

						TextView intRating = new TextView(this);
						intRating
								.setText("Interest Rating: "
										+ ratingsList.get(3));
						intRating.setLayoutParams(rowLayout);
						linearLayout.addView(intRating);
					}

					// Add separator line between rows
					View separator = new View(this);
					separator.setBackgroundColor(Color.DKGRAY);
					RelativeLayout.LayoutParams sepLayout = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					sepLayout.height = 1;
					separator.setLayoutParams(sepLayout);

					linearLayout.addView(separator);
				}
			} else {
				TextView message = new TextView(this);
				message.setText("No ratings found for " + dept + courseNum);
				RelativeLayout.LayoutParams messageLayout = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				messageLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
				messageLayout.addRule(RelativeLayout.CENTER_VERTICAL);
				message.setLayoutParams(messageLayout);
				linearLayout.addView(message);
			}
		} catch (Exception e) {
			Log.e("InitializeViewRatingsPage", e.toString());
			TextView message = new TextView(this);
			message.setText("An error occured when trying to retrieve the ratings\nPlease try again later");
			RelativeLayout.LayoutParams messageLayout = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			messageLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
			messageLayout.addRule(RelativeLayout.CENTER_VERTICAL);
			message.setLayoutParams(messageLayout);
			linearLayout.addView(message);
		}

		state = VIEW_RATING_STATE;
	}

	private void initializeCreateUserPage() {
		final EditText userNameField = (EditText) findViewById(R.id.usernameField);
		final Button submitButton = (Button) findViewById(R.id.createSubmitButton);

		final Spinner majorSpinner = (Spinner) findViewById(R.id.majorSpinner);
		ArrayAdapter<CharSequence> majorAdapter = ArrayAdapter
				.createFromResource(this, R.array.majors,
						android.R.layout.simple_spinner_item);
		majorAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		majorSpinner.setAdapter(majorAdapter);

		final Spinner gradSpinner = (Spinner) findViewById(R.id.gradSpinner);
		ArrayAdapter<CharSequence> gradAdapter = ArrayAdapter
				.createFromResource(this, R.array.grad_years,
						android.R.layout.simple_spinner_item);
		gradAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gradSpinner.setAdapter(gradAdapter);

		final EditText pw1 = (EditText) findViewById(R.id.pw1);
		final EditText pw2 = (EditText) findViewById(R.id.pw2);
		final EditText fName = (EditText) findViewById(R.id.firstName);
		final EditText lName = (EditText) findViewById(R.id.lastName);

		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String newUsername = userNameField.getText().toString();

				if (newUsername.length() > 0) {
					String checkUserUrl = "http://plato.cs.virginia.edu/~cs4720s14carrot/check/"
							+ newUsername;
					try {
						JSONObject result = new WebServiceTask().execute(
								checkUserUrl).get();
						String userExists = result.getString("userexists");

						if (userExists.equals("false")) {
							if (pw1.getText().toString()
									.equals(pw2.getText().toString())) {
								if (pw1.getText().length() >= 8) {
									byte[] salt = new byte[16];
									Random random = new Random(newUsername
											.hashCode());
									random.nextBytes(salt);
									KeySpec spec = new PBEKeySpec(pw1.getText()
											.toString().toCharArray(), salt,
											65536, 128);
									SecretKeyFactory f = SecretKeyFactory
											.getInstance("PBKDF2WithHmacSHA1");
									byte[] hash = f.generateSecret(spec)
											.getEncoded();

									String hashedPassword = new BigInteger(1,
											hash).toString(16);

									String url = "http://plato.cs.virginia.edu/~cs4720s14carrot/addUser";
									Log.i("ADD USER", "ADDING USER");
									JSONObject addUserResult = new WebServiceTask()
											.execute(
													url,
													"username",
													newUsername,
													"password",
													hashedPassword,
													"firstname",
													fName.getText().toString(),
													"lastname",
													lName.getText().toString(),
													"major",
													(String) (majorSpinner
															.getSelectedItem()),
													"graduation",
													(String) (gradSpinner
															.getSelectedItem()))
											.get();
									Log.i("ADD USER", "DONE ADDING USER");
									String success = addUserResult
											.getString("success");
									
									Log.i("CREATING PROJECT", "START");
									String createProjectURL = "http://peppernode.azurewebsites.net/project/add/" + newUsername;
									JSONObject createProjectResult = new WebServiceTask().execute(createProjectURL).get();
									Log.i("CREATING PROJECT", "DONE");
									if (createProjectResult != null)
									{
											int id = createProjectResult.getInt("projectID");
											Log.i("CREATED PROJECT", id + "");
										
											String addProjectURL = "http://plato.cs.virginia.edu/~cs4720s14carrot/addProjectUser/" + newUsername + "/" + id;
											new WebServiceTask().execute(addProjectURL).get();
											
										if (success.equals("true")) {
											Toast toast = Toast.makeText(
													v.getContext(),
													"User successfully created, please login",
													Toast.LENGTH_SHORT);
											toast.show();
										} else {
											Log.i("ERROR", success);

											Toast toast = Toast.makeText(
													v.getContext(), "ERROR: "
															+ success,
													Toast.LENGTH_SHORT);
											toast.show();
										}

										setContentView(R.layout.activity_main);
										initializeLoginPage();
									}
									else
									{
										Toast toast = Toast.makeText(v.getContext(), "CABBAGE ERROR", Toast.LENGTH_SHORT);
										toast.show();
									}
									
								} else {
									pw1.setText("");
									pw2.setText("");

									Toast toast = Toast.makeText(
											v.getContext(),
											"Password must be at least 8 characters",
											Toast.LENGTH_SHORT);
									toast.show();
								}
							} else {
								pw1.setText("");
								pw2.setText("");

								Toast toast = Toast.makeText(v.getContext(),
										"Password Fields Do Not Match!",
										Toast.LENGTH_SHORT);
								toast.show();
							}
						} else {
							userNameField.setText("");

							Toast toast = Toast.makeText(
									v.getContext(),
									"User '" + newUsername + "' already exists",
									Toast.LENGTH_SHORT);
							toast.show();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(userNameField.getWindowToken(), 0);
			}
		});

		state = CREATE_USER_STATE;
	}
	
	private void initializePlanSemesterPage(String dept, String courseNum)
	{
		final EditText classField = (EditText) findViewById(R.id.classField);
		classField.setText(dept + courseNum);
		
		final ArrayList<String> data = new ArrayList<String>();
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
		ListView list = (ListView) findViewById(R.id.listView);
		list.setAdapter(adapter);
		
		try {
			String url = "http://peppernode.azurewebsites.net/project/view/tasks/" + projectId;
			String result = new PlainTextWebService().execute(url).get();
			
			int index = result.indexOf("taskTitle");
			while (index != -1)
			{
				int titleStart = result.indexOf(":", index + 1) + 2;
				int titleEnd = result.indexOf(",", titleStart + 1) - 1;
				String title = result.substring(titleStart, titleEnd);
				if ( ! title.equals("Initiliaze Project") ) {
					data.add(title);
				}
				
				index = result.indexOf("taskTitle", index + 1);
			}
			
			
		} catch (Exception e)
		{
			Log.i("ERROR LOADING PROJECT DATA", e.toString());
		}
		
		
		Button addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (classField.getText().length() > 0)
				{
					String input = classField.getText().toString();
					String url = "http://peppernode.azurewebsites.net/task/add/" + projectId + "/" + input + "/" + "DONE";
					try {
						new PlainTextWebService().execute(url).get();
						classField.setText("");
						
						Toast toast = Toast.makeText(arg0.getContext(), "Class added", Toast.LENGTH_SHORT);
						toast.show();
						
						adapter.add(input);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		
		state = WANT_STATE;
	}
	
	private void initializeAddRatingPage(String user, String startDept, String startCourse) {
		final Spinner deptSpin = (Spinner) findViewById(R.id.deptSpinner);
		final Spinner courseSpin = (Spinner) findViewById(R.id.courseNumSpinner);
		final Spinner profSpin = (Spinner) findViewById(R.id.profSpinner);
		
		final ArrayList<String> depts = new ArrayList<String>();
		final ArrayList<String> courseNums = new ArrayList<String>();
		final ArrayList<String> profNames = new ArrayList<String>();
		
		final ArrayAdapter<String> deptAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depts);
		final ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseNums);
		final ArrayAdapter<String> profAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profNames);
		
		final HashMap<String, HashMap<String, HashMap<String, String>>> data = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		
		isDefaultDeptSet = true;
		final String sDept = startDept;
		final String sCourse = startCourse;
		
		try {
			JSONObject result = new WebServiceTask().execute(
					"http://plato.cs.virginia.edu/~cs4720s14carrot/classesTaken/"
							+ user).get();
			
			if (result != null) {
				Iterator<String> it = (Iterator<String>) (result.keys());

				

				while (it.hasNext()) {
					String className = it.next();
					
					String dept = className.substring(0, className.length() - 4);
					
					HashMap<String, HashMap<String, String>> courseData;
					if (data.containsKey(dept))
					{
						courseData = data.get(dept);
					}
					else
					{
						courseData = new HashMap<String, HashMap<String, String>>();
						data.put(dept, courseData);
					}
					
					String courseNum = className.substring(className.length() - 4);
					HashMap<String, String> profData;
					
					if (courseData.containsKey(courseNum))
					{
						profData = courseData.get(courseNum);
					}
					else
					{
						profData = new HashMap<String, String>();
						courseData.put(courseNum, profData);
					}
					
					String getProfUrl = "http://plato.cs.virginia.edu/~cs4720s14carrot/getProfessorsForCourse/" + dept + "/" + courseNum;
					JSONObject profResult = new WebServiceTask().execute(getProfUrl).get();
					
					if (profResult != null)
					{
						Iterator<String> profIt = (Iterator<String>) (profResult.keys());
						while (profIt.hasNext())
						{
							String profId = profIt.next();
							String profName = (String) (profResult.getString(profId));
							
							profData.put(profId, profName);
						}
					}
				}
				
				Collections.sort(depts);

				String defaultDept = "";
				if (startDept != null)
				{
					defaultDept = startDept;
				}
				
				for (String dept : data.keySet())
				{
					if (defaultDept.length() == 0)
					{
						defaultDept = dept;
					}
					
					depts.add(dept);
				}
				
				String defaultCourse = "";
				if (startCourse != null)
				{
					defaultCourse = startCourse;
				}
				for (String course : data.get(defaultDept).keySet())
				{
					if (defaultCourse.length() == 0)
					{
						defaultCourse = course;
					}
					
					courseNums.add(course);
				}
				
				Collections.sort(courseNums);
				
				for (String profId : data.get(defaultDept).get(defaultCourse).keySet())
				{
					String name = data.get(defaultDept).get(defaultCourse).get(profId);
					if (name.trim().length() == 0)
					{
						name = "Staff";
					}
					
					profNames.add(name);
				}
				
				Collections.sort(profNames);

				deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				deptSpin.setAdapter(deptAdapter);
				deptSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						courseNums.clear();
						profNames.clear();
						
						String dept= (String)deptSpin.getSelectedItem();
												
						String defaultCourse = "";
						for (String course : data.get(dept).keySet())
						{
							if (defaultCourse.length() == 0)
							{
								defaultCourse = course;
							}
							
							courseNums.add(course);
						}
						
						Collections.sort(courseNums);
						courseSpin.setAdapter(courseAdapter);
						
						if (isDefaultDeptSet)
						{
							int coursePos = courseAdapter.getPosition(sCourse);
							if (coursePos > 0)
							{
								courseSpin.setSelection(coursePos);
							}
						}
						Log.i("course spin", "selection overwritten");
						
						for (String profId : data.get(dept).get(defaultCourse).keySet())
						{
							String name = data.get(dept).get(defaultCourse).get(profId);
							if (name.trim().length() == 0)
							{
								name = "Staff";
							}
							
							profNames.add(name);
						}
						
						Collections.sort(profNames);
						profSpin.setAdapter(profAdapter);
						
						isDefaultDeptSet = false;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						
					}
				
				});
				
				deptSpin.setSelection(deptAdapter.getPosition(defaultDept));
				
				
				courseSpin.setAdapter(courseAdapter);
				courseSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						profNames.clear();
						
						String dept = (String)deptSpin.getSelectedItem();
						String course = (String)courseSpin.getSelectedItem();
						for (String profId : data.get(dept).get(course).keySet())
						{
							String name = data.get(dept).get(course).get(profId);
							if (name.trim().length() == 0)
							{
								name = "Staff";
							}
							
							profNames.add(name);
						}
						
						Collections.sort(profNames);
						profSpin.setAdapter(profAdapter);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						
					}
				});
				
				int coursePos = courseAdapter.getPosition(defaultCourse);
				
				
				
				courseSpin.setSelection(coursePos);
				Log.i("course spin", "selection set");
				profSpin.setAdapter(profAdapter);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final SeekBar instSeek = (SeekBar) findViewById(R.id.instructorRatingSeek);
		final TextView instValue = (TextView) findViewById(R.id.instructorRatingValue);
		instSeek.setProgress(50);
		instValue.setText("50");
		instSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				instValue.setText(progress + "");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});

		final SeekBar diffSeek = (SeekBar) findViewById(R.id.difficultRatingSeek);
		final TextView diffValue = (TextView) findViewById(R.id.difficultRatingValue);
		diffSeek.setProgress(50);
		diffValue.setText("50");
		diffSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				diffValue.setText(progress + "");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});

		final SeekBar timeSeek = (SeekBar) findViewById(R.id.timeRatingSeek);
		final TextView timeValue = (TextView) findViewById(R.id.timeRatingValue);
		timeSeek.setProgress(50);
		timeValue.setText("50");
		timeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				timeValue.setText(progress + "");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});

		final SeekBar interSeek = (SeekBar) findViewById(R.id.interestRatingSeek);
		final TextView interValue = (TextView) findViewById(R.id.interestRatingValue);
		interSeek.setProgress(50);
		interValue.setText("50");
		interSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				interValue.setText(progress + "");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});

		final Button submit = (Button) findViewById(R.id.submitRatingButton);
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				double instValue = instSeek.getProgress() / 20.0;
				double diffValue = diffSeek.getProgress() / 20.0;
				double timeValue = timeSeek.getProgress() / 20.0;
				double interValue = interSeek.getProgress() / 20.0;
				
				String dept = (String)deptSpin.getSelectedItem();
				String courseNum = (String)courseSpin.getSelectedItem();
				String profName = (String)profSpin.getSelectedItem();
				
				HashMap<String, String> profData = data.get(dept).get(courseNum);
				
				String profId = "";
				for (String pId : profData.keySet())
				{
					if (profData.get(pId).equals(profName) || (profName.equals("Staff") && profData.get(pId).trim().length() == 0))
					{
						profId = pId;
						break;
					}
				}
				
				try {
					// TODO update to correct URL
					String checkPrevRatingURL = "http://plato.cs.virginia.edu/~cs4720s14carrot/hasrated/" + username + "/" + dept + "/" + courseNum;
					JSONObject checkResult = new WebServiceTask().execute(checkPrevRatingURL).get();
					
					if (!checkResult.getBoolean("hasrated"))
					{
						String url = "http://plato.cs.virginia.edu/~cs4720s14carrot/rate/"
								+ username
								+ "/"
								+ dept
								+ "/"
								+ courseNum
								+ "/"
								+ profId
								+ "/"
								+ instValue
								+ "/"
								+ diffValue
								+ "/"
								+ timeValue
								+ "/" + interValue;
						
						new WebServiceTask().execute(url).get();
					}
					else
					{
						Toast toast = Toast.makeText(v.getContext(), "You have already rated this class!\nRating not submitted", Toast.LENGTH_SHORT);
						toast.show();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//setContentView(R.layout.view_classes);
				//initializeViewClassesPage();
				setContentView(R.layout.add_class_form);
				initializeAddClassForm(" ", " ");
			}
		});
		
		state = PICK_RATING_STATE;
	}
	
	@Override
	public void onBackPressed() {
		Log.i("Back button pressed", "button pressed");

		switch (state) {
		case LOGIN_STATE:
			super.onBackPressed();
			break;
		case MAIN_STATE:
		case CREATE_USER_STATE:
			setContentView(R.layout.activity_main);
			initializeLoginPage();
			break;
		case ADD_CLASS_STATE:
		case REQ_VIEW_STATE:
		case VIEW_CLASSES_TAKEN_STATE:
		case RATING_FORM_STATE:
		case VIEW_RATING_STATE:
		case PLAN_STATE:
		case PICK_RATING_STATE:
		case WANT_STATE:
			setContentView(R.layout.requirements_page);
			initializeRequirementsViewPage();
			break;
		default:
			super.onBackPressed();
			break;
		}
	}
}
