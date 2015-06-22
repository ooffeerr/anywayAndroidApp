package il.co.anyway.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class TutorialActivity extends FragmentActivity {

    private TextView mNextTv;
    private ImageView mDot1, mDot2, mDot3, mDot4, mDot5;

    private static final int NUMBER_OF_TABS = 5;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // get UI widgets
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mNextTv = (TextView)findViewById(R.id.textViewNext);
        mDot1 = (ImageView) findViewById(R.id.dotView1);
        mDot2 = (ImageView) findViewById(R.id.dotView2);
        mDot3 = (ImageView) findViewById(R.id.dotView3);
        mDot4 = (ImageView) findViewById(R.id.dotView4);
        mDot5 = (ImageView) findViewById(R.id.dotView5);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {

                    case 4:
                        mDot1.setImageResource(R.drawable.dot_selected);
                        mDot2.setImageResource(R.drawable.dot_not_selected);
                        mDot3.setImageResource(R.drawable.dot_not_selected);
                        mDot4.setImageResource(R.drawable.dot_not_selected);
                        mDot5.setImageResource(R.drawable.dot_not_selected);
                        mNextTv.setText(R.string.next);
                        break;

                    case 3:
                        mDot1.setImageResource(R.drawable.dot_not_selected);
                        mDot2.setImageResource(R.drawable.dot_selected);
                        mDot3.setImageResource(R.drawable.dot_not_selected);
                        mDot4.setImageResource(R.drawable.dot_not_selected);
                        mDot5.setImageResource(R.drawable.dot_not_selected);
                        mNextTv.setText(R.string.next);
                        break;

                    case 2:
                        mDot1.setImageResource(R.drawable.dot_not_selected);
                        mDot2.setImageResource(R.drawable.dot_not_selected);
                        mDot3.setImageResource(R.drawable.dot_selected);
                        mDot4.setImageResource(R.drawable.dot_not_selected);
                        mDot5.setImageResource(R.drawable.dot_not_selected);
                        mNextTv.setText(R.string.next);
                        break;

                    case 1:
                        mDot1.setImageResource(R.drawable.dot_not_selected);
                        mDot2.setImageResource(R.drawable.dot_not_selected);
                        mDot3.setImageResource(R.drawable.dot_not_selected);
                        mDot4.setImageResource(R.drawable.dot_selected);
                        mDot5.setImageResource(R.drawable.dot_not_selected);
                        mNextTv.setText(R.string.next);
                        break;

                    case 0:
                        mDot1.setImageResource(R.drawable.dot_not_selected);
                        mDot2.setImageResource(R.drawable.dot_not_selected);
                        mDot3.setImageResource(R.drawable.dot_not_selected);
                        mDot4.setImageResource(R.drawable.dot_not_selected);
                        mDot5.setImageResource(R.drawable.dot_selected);
                        mNextTv.setText(R.string.done);
                        break;

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // set the last tab as first to force right to left behaviour
        mViewPager.setCurrentItem(NUMBER_OF_TABS-1);
    }

    public void skipTutorial(View view) {
        finish();
    }

    public void nextTutorialPage(View view) {
        int currentTab = mViewPager.getCurrentItem();

        if (currentTab > 0)
            mViewPager.setCurrentItem(currentTab-1);
        else if (currentTab == 0)
            finish();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return NUMBER_OF_TABS;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Bundle fragmentsArgs = getArguments();
            int pageViewPosition = fragmentsArgs.getInt(ARG_SECTION_NUMBER, -1);

            int pageLayout = R.layout.fragment_tutorial_a;
            switch (pageViewPosition) {

                case 1: pageLayout = R.layout.fragment_tutorial_e;
                    break;

                case 2: pageLayout = R.layout.fragment_tutorial_d;
                    break;

                case 3: pageLayout = R.layout.fragment_tutorial_c;
                    break;

                case 4: pageLayout = R.layout.fragment_tutorial_b;
                    break;

            }

            return inflater.inflate(pageLayout, container, false);
        }
    }

}
