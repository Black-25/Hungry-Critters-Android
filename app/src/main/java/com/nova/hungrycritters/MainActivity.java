package com.nova.hungrycritters;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(12, 17, 35));
        getWindow().setNavigationBarColor(Color.rgb(12, 17, 35));
        setContentView(new GameView());
    }

    static class Food {
        float x, y, speed, size;
        int color;
        String emoji;

        Food(float x, float y, float speed, int color,
             String emoji, float size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.color = color;
            this.emoji = emoji;
            this.size = size;
        }
    }

    class GameView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler handler = new Handler();
        private final Random random = new Random();
        private final ArrayList<Food> foods = new ArrayList<>();

        private float playerX;
        private float playerY;
        private float playerRadius = 36;

        private int score = 0;
        private int lives = 3;
        private int frames = 0;

        private boolean gameOver = false;
        private boolean started = false;

        private final int[] foodColors = {
            Color.rgb(255, 75, 92),
            Color.rgb(255, 190, 60),
            Color.rgb(75, 220, 145),
            Color.rgb(90, 160, 255),
            Color.rgb(205, 110, 255)
        };

        private final String[] foodEmoji = {
            "🍎", "🥕", "🍓", "🍌", "🍇"
        };

        private final Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    updateGame();
                    invalidate();
                    handler.postDelayed(this, 30);
                }
            }
        };

        GameView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setFocusable(true);
        }

        private void startGame() {
            handler.removeCallbacks(gameLoop);

            score = 0;
            lives = 3;
            frames = 0;
            gameOver = false;
            started = true;

            foods.clear();

            playerX = getWidth() / 2f;
            playerY = getHeight() - 125;

            handler.post(gameLoop);
            invalidate();
        }

        private void updateGame() {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            frames++;

            int spawnInterval = Math.max(14, 35 - score / 10);

            if (frames % spawnInterval == 0) {
                float size = 23 + random.nextInt(10);

                foods.add(new Food(
                    size + random.nextFloat()
                        * Math.max(1, getWidth() - size * 2),
                    -30,
                    4 + random.nextFloat() * 3 + score * 0.015f,
                    foodColors[random.nextInt(foodColors.length)],
                    foodEmoji[random.nextInt(foodEmoji.length)],
                    size
                ));
            }

            Iterator<Food> iterator = foods.iterator();

            while (iterator.hasNext()) {
                Food food = iterator.next();
                food.y += food.speed;

                float dx = food.x - playerX;
                float dy = food.y - playerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < playerRadius + food.size) {
                    score += 5;
                    iterator.remove();
                    continue;
                }

                if (food.y > getHeight() + food.size) {
                    lives--;
                    iterator.remove();

                    if (lives <= 0) {
                        gameOver = true;
                        break;
                    }
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            if (width <= 0 || height <= 0) return;

            if (!started) {
                playerX = width / 2f;
                playerY = height - 125;
            }

            // Background
            canvas.drawColor(Color.rgb(12, 17, 35));

            // Decorative stars
            paint.setColor(Color.rgb(50, 65, 100));

            for (int i = 0; i < 28; i++) {
                float x = (i * 137f) % width;
                float y = (i * 97f) % height;
                canvas.drawCircle(x, y, 2, paint);
            }

            drawHeader(canvas, width);
            drawFoods(canvas);
            drawRabbit(canvas);

            if (!started) {
                drawStartScreen(canvas, width, height);
            } else if (gameOver) {
                drawGameOver(canvas, width, height);
            }
        }

        private void drawHeader(Canvas canvas, int width) {
            paint.setColor(Color.rgb(25, 34, 60));

            canvas.drawRoundRect(
                new RectF(18, 20, width - 18, 112),
                22, 22, paint
            );

            paint.setTextSize(20);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("HUNGRY CRITTERS", 34, 52, paint);

            paint.setTextSize(17);
            paint.setColor(Color.rgb(100, 180, 255));
            canvas.drawText("SCORE: " + score, 34, 83, paint);

            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setColor(Color.rgb(255, 105, 125));
            canvas.drawText("♥ ".repeat(Math.max(0, lives)),
                width - 34, 83, paint);

            paint.setTextAlign(Paint.Align.LEFT);
        }

        private void drawFoods(Canvas canvas) {
            for (Food food : foods) {
                paint.setColor(food.color);
                paint.setAlpha(65);

                canvas.drawCircle(
                    food.x, food.y, food.size + 7, paint
                );

                paint.setAlpha(255);
                paint.setTextSize(food.size * 1.6f);
                paint.setTextAlign(Paint.Align.CENTER);

                canvas.drawText(
                    food.emoji, food.x, food.y + food.size * 0.55f,
                    paint
                );
            }

            paint.setTextAlign(Paint.Align.LEFT);
        }

        private void drawRabbit(Canvas canvas) {
            canvas.save();
            canvas.translate(playerX, playerY);

            // Ears
            paint.setColor(Color.rgb(245, 235, 225));
            canvas.drawRoundRect(
                new RectF(-23, -52, -5, -12), 12, 12, paint
            );
            canvas.drawRoundRect(
                new RectF(5, -52, 23, -12), 12, 12, paint
            );

            // Inner ears
            paint.setColor(Color.rgb(255, 150, 175));
            canvas.drawRoundRect(
                new RectF(-18, -46, -10, -17), 6, 6, paint
            );
            canvas.drawRoundRect(
                new RectF(10, -46, 18, -17), 6, 6, paint
            );

            // Body
            paint.setColor(Color.rgb(245, 235, 225));
            canvas.drawCircle(0, 8, 31, paint);

            // Eyes
            paint.setColor(Color.rgb(35, 35, 50));
            canvas.drawCircle(-11, 0, 4, paint);
            canvas.drawCircle(11, 0, 4, paint);

            // Nose
            paint.setColor(Color.rgb(255, 115, 155));
            canvas.drawCircle(0, 10, 4, paint);

            // Mouth
            paint.setColor(Color.rgb(100, 70, 80));
            paint.setStrokeWidth(2);
            canvas.drawLine(0, 14, -5, 18, paint);
            canvas.drawLine(0, 14, 5, 18, paint);

            // Feet
            paint.setColor(Color.rgb(245, 235, 225));
            canvas.drawOval(
                new RectF(-29, 28, -3, 42), paint
            );
            canvas.drawOval(
                new RectF(3, 28, 29, 42), paint
            );

            canvas.restore();
        }

        private void drawStartScreen(
            Canvas canvas, int width, int height
        ) {
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(29);

            canvas.drawText(
                "Hungry Critters", width / 2f, height * 0.35f, paint
            );

            paint.setTypeface(android.graphics.Typeface.DEFAULT);
            paint.setTextSize(17);
            paint.setColor(Color.rgb(185, 200, 225));

            canvas.drawText(
                "Catch the falling food!", width / 2f,
                height * 0.35f + 42, paint
            );

            drawButton(
                canvas, width / 2f, height * 0.48f,
                "TAP TO START"
            );

            paint.setTextSize(15);
            paint.setColor(Color.LTGRAY);

            canvas.drawText(
                "Drag the rabbit to catch food",
                width / 2f, height * 0.48f + 65, paint
            );

            paint.setTextAlign(Paint.Align.LEFT);
        }

        private void drawGameOver(
            Canvas canvas, int width, int height
        ) {
            paint.setColor(Color.argb(210, 5, 8, 20));
            canvas.drawRect(0, 0, width, height, paint);

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(34);
            paint.setColor(Color.rgb(255, 100, 125));

            canvas.drawText(
                "GAME OVER", width / 2f, height * 0.40f, paint
            );

            paint.setTextSize(23);
            paint.setColor(Color.WHITE);

            canvas.drawText(
                "Your Score: " + score, width / 2f,
                height * 0.40f + 48, paint
            );

            drawButton(
                canvas, width / 2f, height * 0.53f,
                "PLAY AGAIN"
            );

            paint.setTextAlign(Paint.Align.LEFT);
        }

        private void drawButton(
            Canvas canvas, float centerX, float centerY, String text
        ) {
            paint.setColor(Color.rgb(90, 105, 255));

            canvas.drawRoundRect(
                new RectF(
                    centerX - 115, centerY - 25,
                    centerX + 115, centerY + 25
                ),
                18, 18, paint
            );

            paint.setColor(Color.WHITE);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(17);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(text, centerX, centerY + 6, paint);

            paint.setTextAlign(Paint.Align.LEFT);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!started || gameOver) {
                    startGame();
                    return true;
                }

                playerX = Math.max(
                    playerRadius,
                    Math.min(getWidth() - playerRadius, x)
                );

                invalidate();
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (started && !gameOver) {
                    playerX = Math.max(
                        playerRadius,
                        Math.min(getWidth() - playerRadius, x)
                    );

                    invalidate();
                }

                return true;
            }

            return true;
        }

        @Override
        protected void onDetachedFromWindow() {
            handler.removeCallbacks(gameLoop);
            super.onDetachedFromWindow();
        }
    }
                      }
