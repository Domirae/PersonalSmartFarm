package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.w3c.dom.Text;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    //캘린더 객체
    MaterialCalendarView materialCalendarView;
    ArrayList<CalendarDay> dates; //캘린더에 내용 표시
    ArrayList<EventObject> eventObjects;//이벤트 객체를 저장하는 리스트
    int growthDay; // 재배기간을 저장하는 영역

    //슬라이드 페이지 만드는 부분
    TextView tv_selected_date,eventTextview;
    CheckBox start_growingCheckBox;

    Calendar calendarOriginDate;//원래 초기에 getInstance로 설정되는 부분
    CalendarDay calendarMarkDate;//캘린더 선택 부분

    Button testBtn;
    User app_user;
    private   static   final   String  SERVER_ADDRESS = "http://ec2-52-192-209-67.ap-northeast-1.compute.amazonaws.com";

    final static String TAG = "CALERDAR_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //초기 등록 및 설정 부분
        //슬라이드  상위 부분
        tv_selected_date = (TextView)findViewById(R.id.tv_selected_date);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String str_date = df.format(new Date());

        tv_selected_date.setText(str_date);
        calendarOriginDate=Calendar.getInstance(); // 캘린더 인스턴스를 가져오며 현재 시스템 날짜와 시간을 받아온다.
        eventTextview = (TextView)findViewById(R.id.eventTextview);
        start_growingCheckBox = (CheckBox)findViewById(R.id.start_growingCheckBox);
        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        testBtn = (Button)findViewById(R.id.str_growingBtn);

        eventObjects=new ArrayList<>();

        //materialCalender 부분의 상태 초기설정 부분
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setMinimumDate(CalendarDay.from(1999, 1, 1))
                .setMaximumDate(CalendarDay.from(2100, 12, 31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        //Date부분을 클릭하면 발생하는 이벤트 처리 부분
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                calendarMarkDate= date; //선택한 날짜를 받아와서 진행하기
                tv_selected_date.setText(date.getDate().toString());

                if(eventObjects.size()!=0) {
                    //구조가 맞는지 생각해보고 여쭤보기
                    Iterator iterator = eventObjects.iterator();

                    while (iterator.hasNext()) {
                        EventObject eventObject = (EventObject) iterator.next();
                        CalendarDay calendarDay = eventObject.getCalendarDay();

                        if(calendarDay.getDay()==date.getDay()){
                            eventTextview.setText(eventObject.getEvent_content().toString());
                        }else{
                            eventTextview.setText("");
                        }
                    }
                }

                //materialCalendarView.addDecorator(new ClickDecorator(date));
            }
        });

        //***캘린더에 표시하기 -> 메인에서 전달된 내용을 받아와 처리한다.
        Intent intent = getIntent();
        if (intent != null) {

            app_user = (User)intent.getSerializableExtra("app_user");
            String date = intent.getStringExtra("date");
            growthDay=Integer.parseInt(app_user.getGrowth_span());

            try{

                String start_day = app_user.getStart_date();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                calendarOriginDate.setTime(transFormat.parse(start_day));

            }catch(ParseException e){
                e.printStackTrace();
            }
            Log.d(TAG,""+app_user.isToday_register());
            boolean today_register = app_user.isToday_register();

            if(today_register){
                calendar_markingDate(calendarOriginDate,growthDay); //바로 전달된 날짜로 등록하도록 함
                Log.d("calendarOriginDate",calendarOriginDate.toString());
            }else{
                int growthDay = 0; //지역변수로 재배날짜를 0으로 설정하고 보낸다.
                calendar_markingDate(calendarOriginDate,growthDay);
            }
        }

        event_createAndshow(); //캘린더 이벤트 생성 부분--> 나중에 스레드로 구현해서 알람이 뜨도록 테스트해볼 예정-> 다시 해봐야함

        //***재배 기간 체크 박스 이벤트 처리 부분 --> 버튼으로 설정하든가 아니면 동기화 설정을 해야할 것같음
        start_growingCheckBox.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(start_growingCheckBox.isChecked()){
                    //기존의 dates를 별도의 Array에 저장하도록 한다.
                    ArrayList<CalendarDay> extra_dates =dates;

                    //클릭된 날짜가 포함된 새로운 dates 객체를 생성 후 달력에 표현하도록 한다.
                    calendarOriginDate.set(calendarMarkDate.getYear(),calendarMarkDate.getMonth(),calendarMarkDate.getDay());
                    calendar_markingDate(calendarOriginDate,growthDay);

                    //extra_dates를 이용하여 기존에 있었던 내용을 지우는 Decorator를 제작하며, 이벤트가 있는 날은 제외하는 방식으로 진행한다.
                    materialCalendarView.addDecorator(new EraseDecorator(extra_dates));
                }
            }
        } );

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<CalendarDay> extra_dates =dates;
                materialCalendarView.addDecorator(new EraseDecorator(extra_dates));  //extra_dates를 이용하여 기존에 있었던 내용을 지우는 Decorator를 제작하며, 이벤트가 있는 날은 제외하는 방식으로 진행한다.
                //클릭된 날짜가 포함된 새로운 dates 객체를 생성 후 달력에 표현하도록 한다.
                calendarOriginDate.set(calendarMarkDate.getYear(),calendarMarkDate.getMonth(),calendarMarkDate.getDay());
                calendar_markingDate(calendarOriginDate,growthDay);

                //app_user 객체의 내용 역시 변경하며, 동시에 DB 문도 변경될 수 있도록 설정한다.
                app_user.setToday_register(true);
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                String selected_start_day = transFormat.format(calendarMarkDate.getDate());

                app_user.setStart_date(selected_start_day);
                UpdateDayThread updateDayThread = new UpdateDayThread(selected_start_day);
                updateDayThread.start();

                Log.d("selected_day",selected_start_day);
            }
        });

    }

    public class UpdateDayThread extends Thread{
        String update_day;

        public UpdateDayThread(String update_day){
            this.update_day = update_day;
        }

        public void run(){
            try{
                Log.d(TAG,app_user.getId());
                URL url = new URL(SERVER_ADDRESS+"/Login/updateUserStartDay.php?"+"id="+app_user.getId()+"&growth_start="+update_day);
                url.openStream(); //서버에 있는 php파일에 id와 password를 인자로 입력하는 구문
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /*class 부분 */
    //내가 설정한 기간에 따라 dot로 표시해주는 EventDecorator
    private class ScheduleDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public ScheduleDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            //view.addSpan(new ForegroundColorSpan(color));
            view.setBackgroundDrawable(ContextCompat.getDrawable(CalendarActivity.this,R.drawable.selector3));
        }
    }

    //토요일 날짜 표시
    public class SaturdayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();

        public SaturdayDecorator() {
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SATURDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.BLUE));
        }
    }

    //일요일 부분을 빨간색으로 처리하는 부분 --> 나중에 토요일 처리하는 부분과 합칠 예정
    public class SundayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();

        public SundayDecorator() {
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.RED));
        }
    }

    //오늘 날짜에 대해서 강조하기 위한 class
    public class ToDayDecorator implements DayViewDecorator {

        private CalendarDay date;

        public ToDayDecorator() {
            date = CalendarDay.today();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return date != null && day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StyleSpan(Typeface.BOLD));
//            view.addSpan(new RelativeSizeSpan(1.4f));
//            view.addSpan(new ForegroundColorSpan(Color.GREEN));
            view.setBackgroundDrawable(ContextCompat.getDrawable(CalendarActivity.this,R.drawable.selector));
        }

        public void setDate(Date date) {
            this.date = CalendarDay.from(date);
        }
    }

    // 날짜와 해당 이벤트 내용을 저장하는 객체
    public class EventObject{

        CalendarDay date;
        String event_content;

        public EventObject(CalendarDay date,String event_content){
            this.date=date;
            this.event_content=event_content;
        }

        public CalendarDay getCalendarDay(){
            return date;
        }

        public String getEvent_content(){
            return event_content;
        }

    }

    //이벤트 발생시 표시하는 부분
    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<EventObject> eventObjects;
        private HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<EventObject> eventObjects) {
            this.color = color;
            this.eventObjects = new HashSet<>(eventObjects);
            dates = new HashSet<>();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {

            //해당 객체에서 Calendarday의 값을 가져온다.
            Iterator iterator = eventObjects.iterator();

            while(iterator.hasNext()){
                EventObject eventObject = (EventObject) iterator.next();
                CalendarDay calendarDay =eventObject.getCalendarDay();
                dates.add(calendarDay);
            }

            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
            view.addSpan(new ForegroundColorSpan(color));
        }

    }

    //Decorator를 지우는 부분
    public class EraseDecorator implements DayViewDecorator{

        private HashSet<CalendarDay> erase_dates;

        public EraseDecorator(Collection<CalendarDay> erase_dates){
            this.erase_dates = new HashSet<>(erase_dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            //해당 날짜를 iterator을 이용하여 다시 표시 할 수 있도록 함  -> 이벤트는 제외하고 표시 할 수 있도록 함
            return erase_dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(CalendarActivity.this,R.drawable.selector4));
        }
    }


    /*메소드 부분*/
    //날짜를 달력에 표시하는 부분
    public void calendar_markingDate(Calendar calendar,int growthDay){

        dates = new ArrayList<>();
        calendar.add(Calendar.MONTH, 0);//현재 달에 표시한다.

        for(int i=0;i<growthDay;i++){
            //캘린더로 부터 해당 날짜의 날들을 받아온다.
            CalendarDay day = CalendarDay.from(calendar);
            dates.add(day); //해당 날들을 모두 담는다....?
            calendar.add(Calendar.DATE, 1);
        }

        materialCalendarView.addDecorator(new ScheduleDecorator(Color.GREEN,dates));
    }

    //이벤트 객체 생성 후 달력에 등록하는 메소드
    public void event_createAndshow(){
        //캘린더 이벤트 생성 부분--> 나중에 스레드로 구현해서 알람이 뜨도록 테스트해볼 예정
        Calendar sensor_event1=Calendar.getInstance();
        Calendar sensor_event2=Calendar.getInstance();

        sensor_event1.add(Calendar.DATE,5);
        sensor_event2.add(Calendar.DATE,8);

        CalendarDay sensor_event_day1 = CalendarDay.from(sensor_event1);
        CalendarDay sensor_event_day2 = CalendarDay.from(sensor_event2);

        EventObject event1= new EventObject(sensor_event_day1,"새싹이 나기 시작했어요!");
        EventObject event2 = new EventObject(sensor_event_day2,"열매가 자랐어요");

        eventObjects.add(event1);
        eventObjects.add(event2);

        materialCalendarView.addDecorator(new EventDecorator(Color.argb(255,128,65,217),eventObjects));
        materialCalendarView.addDecorators(new ToDayDecorator());
        materialCalendarView.addDecorator(new SundayDecorator());
    }
}