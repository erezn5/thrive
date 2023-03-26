package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.awaitility.Duration;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class DatePicker extends PageElements {

    @FindBy(css = "[cywapp-id='class_date_start']")
    private WebElement startDateElm;
    @FindBy(css = "[cywapp-id='class_date_end']")
    private WebElement endDateElm;
    @FindBy(css = "div[class*='menuable'] td button")
    protected List<WebElement> datesElms;
    @FindBy(css = "[cywapp-id='date-month-previous']")
    private WebElement previousMonthBtnElm;
    @FindBy(css = "[cywapp-id='date-month-header']")
    protected WebElement monthBtnElm;
    @FindBy(css = "[cywapp-id='date-month-next']")
    private WebElement nextMonthBtnElm;
    @FindBy(css = "div[class*='v-date-picker-table'] tr:nth-child(1) td:nth-child(1)")
    private WebElement januaryBtnElm;

    private final Calendar calendar;
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMM");

    {
        calendar = Calendar.getInstance(TimeZone.getDefault());
    }

    public DatePicker(DriverFactory driver) {
        super(driver);
    }

    public void setStartDate(String dayToSelect) {
        scrollElmIntoView(startDateElm);
        clickElm(startDateElm);
//        if(calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
//            verifyElm(previousMonthBtnElm);
//            clickFirstDayOfMonth();
//        }else {
        setDateRange(dayToSelect);
//        }
    }

    public void setEndDate(String day, boolean... sameDayFlag) {
        clickElm(endDateElm);
        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
            verifyAndClickElm(nextMonthBtnElm);
            if (day.equals("01")) {
                setDateRange("01");
            }
            if (sameDayFlag.length != 0 && sameDayFlag[0]) {
                setDateRange("01");
            }
//            else{
//                setDateRange("02");
//            }
        } else {
            //click to move to next year if it is the last day of the year
            verifyEndOfYearAndEndOfMonth(day);
        }
    }

    private void setDateRange(String dayToSelect) {
        if (!dayToSelect.equals("")) {
            if (dayToSelect.charAt(0) == '0') {
                dayToSelect = dayToSelect.split("0")[1];
            }
            clickOnElmFromList(datesElms, dayToSelect);
        }
    }

    private void verifyEndOfYearAndEndOfMonth(String day) {
        if (checkLastDayOfYear()) {
            clickElm(monthBtnElm).clickElm(nextMonthBtnElm).clickElm(januaryBtnElm);//move to next year and click on january
        }
        //click to move to the next month if it is the last day of the month
        if (checkLastDayOfMonth()) {
            clickFirstDayOfMonth();
        } else {
            setDateRange(day);
        }
    }

    private void clickFirstDayOfMonth() {
        verifyAndClickElm(nextMonthBtnElm);//move to next month
        sleep(Duration.FIVE_HUNDRED_MILLISECONDS);
        setDateRange("01");
    }

    private boolean checkLastDayOfMonth() {
//        calendar.add(Calendar.DATE, 1);
        return calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE);
    }

    private boolean checkLastDayOfYear() {
        String monthStr = monthFormat.format(calendar.getTime());
        String dayStr = dayFormat.format(calendar.getTime());
        return monthStr.equals("December") && dayStr.equals("31");
    }

}
