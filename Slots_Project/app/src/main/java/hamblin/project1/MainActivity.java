package hamblin.project1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView flower1;
    ImageView flower2;
    ImageView flower3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user clicks the Go button */
    public void goButton(View view) {
        ImageView resetView = (ImageView) findViewById(R.id.reset_image);
        resetView.setVisibility(View.VISIBLE);

        flower1 = (ImageView) findViewById(R.id.flower1);
        flower2 = (ImageView) findViewById(R.id.flower2);
        flower3 = (ImageView) findViewById(R.id.flower3);

        final ImageView[] flowers = {flower1, flower2, flower3};
        final int[] flower_count = {0, 0, 0};

        final RotateAnimation rotateAnimation = new RotateAnimation(0, 1440, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flower1.setImageResource(R.drawable.tmp);
                flower2.setImageResource(R.drawable.tmp);
                flower3.setImageResource(R.drawable.tmp);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                for(final ImageView f : flowers) {
                    Random r = new Random();
                    switch (r.nextInt(3) + 1) {
                        case 1:
                            f.setImageResource(R.drawable.f1);
                            flower_count[0]++;
                            break;
                        case 2:
                            f.setImageResource(R.drawable.f2);
                            flower_count[1]++;
                            break;
                        case 3:
                            f.setImageResource(R.drawable.f3);
                            flower_count[2]++;
                            break;
                    }
                }

                TextView coinAmount = (TextView) findViewById(R.id.coin_amount);
                int currentAmount = Integer.parseInt(coinAmount.getText().toString().substring(2));

                int addedAmount = 0;
                for(int i : flower_count) {
                    if (i > 1) {
                        addedAmount = i;
                        break;
                    }
                }

                int latestAmount = currentAmount+addedAmount-CONSTANTS.COST_PER_ROLL;
                String newAmount = "$ " + latestAmount;
                if(latestAmount == CONSTANTS.YOUR_BROKE) {
                    ImageView goView = (ImageView) findViewById(R.id.go_image);
                    goView.setVisibility(View.INVISIBLE);
                }
                coinAmount.setText(newAmount);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        flower1.startAnimation(rotateAnimation);
        flower2.startAnimation(rotateAnimation);
        flower3.startAnimation(rotateAnimation);
    }

    public void resetButton(View view) {
        ImageView resetView = (ImageView) findViewById(R.id.reset_image);
        resetView.setVisibility(View.INVISIBLE);
        ImageView goView = (ImageView) findViewById(R.id.go_image);
        goView.setVisibility(View.VISIBLE);
        TextView coinAmount = (TextView) findViewById(R.id.coin_amount);
        coinAmount.setText(R.string.coin_amount);

        final ImageView flower1 = (ImageView) findViewById(R.id.flower1);
        final ImageView flower2 = (ImageView) findViewById(R.id.flower2);
        final ImageView flower3 = (ImageView) findViewById(R.id.flower3);

        flower1.setImageResource(R.drawable.tmp);
        flower2.setImageResource(R.drawable.tmp);
        flower3.setImageResource(R.drawable.tmp);
    }
}
