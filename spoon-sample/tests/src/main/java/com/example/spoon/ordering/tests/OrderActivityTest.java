package com.example.spoon.ordering.tests;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.CheckedTextView;
import com.robotium.solo.Solo;
import com.squareup.spoon.Spoon;
import com.example.spoon.ordering.OrderActivity;

import static org.fest.assertions.api.ANDROID.assertThat;

public class OrderActivityTest extends ActivityInstrumentationTestCase2<OrderActivity> {
  public OrderActivityTest() {
    super(OrderActivity.class);
  }

  private OrderActivity activity;
  private Instrumentation instrumentation;
  private Solo solo;

  @Override protected void setUp() throws Exception {
    super.setUp();
    activity = getActivity();
	instrumentation = getInstrumentation();
    solo = new Solo(instrumentation, activity);
  }

  public void testMakeASandwich_ItTastesGood() {
    Button nextButton = (Button) solo.getText("Next");

    Spoon.screenshot(activity, instrumentation, "initial_state");

    assertThat(nextButton).isDisabled();
    solo.clickOnText("Sandwich");
    Spoon.screenshot(activity, instrumentation, "selected_sandwich");
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "bread");

    assertThat(nextButton).isDisabled();
    solo.clickOnText("Wheat");
    Spoon.screenshot(activity, instrumentation, "selected_bread");
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "meats");

    assertThat(nextButton).isEnabled();
    solo.clickOnText("Turkey");
    solo.clickOnText("Roast Beef");
    Spoon.screenshot(activity, instrumentation, "selected_meats");

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "veggies");

    assertThat(nextButton).isEnabled();
    solo.clickOnText("Lettuce");
    solo.clickOnText("Tomato");
    solo.clickOnText("Cucumbers");
    Spoon.screenshot(activity, instrumentation, "selected_veggies");

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "cheeses");

    assertThat(nextButton).isEnabled();
    solo.clickOnText("American");
    Spoon.screenshot(activity, instrumentation, "selected_cheeses");

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "toasted");

    CheckedTextView toastedNo = (CheckedTextView) solo.getText("No");
    assertThat(toastedNo).isChecked();
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "your_info");

    assertThat(nextButton).isDisabled();
    solo.typeText(0, "Trent Sondag");
    solo.typeText(1, "bearfight@example.com");
    Spoon.screenshot(activity, instrumentation, "completed_your_info");
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "review");
  }

  public void testMakeSomeSalad_ItIsHealthy() {
    Button nextButton = (Button) solo.getText("Next");

    Spoon.screenshot(activity, instrumentation, "initial_state");

    assertThat(nextButton).isDisabled();
    solo.clickOnText("Salad");
    Spoon.screenshot(activity, instrumentation, "selected_salad");
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "salad_type");

    assertThat(nextButton).isDisabled();
    solo.clickOnText("Caesar");
    Spoon.screenshot(activity, instrumentation, "selected_salad_type");
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "dressing");

    CheckedTextView dressingNone = (CheckedTextView) solo.getText("No dressing");
    assertThat(dressingNone).isChecked();
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "your_info");

    assertThat(nextButton).isDisabled();
    solo.typeText(0, "Trent Sondag");
    solo.typeText(1, "bearfight@example.com");
    Spoon.screenshot(activity, instrumentation, "completed_your_info");
    assertThat(nextButton).isEnabled();

    solo.clickOnView(nextButton);
    Spoon.screenshot(activity, instrumentation, "review");
  }
}
