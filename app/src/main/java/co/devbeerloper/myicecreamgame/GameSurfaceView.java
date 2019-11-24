package co.devbeerloper.myicecreamgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameSurfaceView extends SurfaceView implements Runnable {

    private Context context;
    private SurfaceHolder holder;
    private Canvas canvas;
    private Paint paint;
    private float screenWidth;
    private float screenHeight;

    Random random = new Random();
    int frameCount;
    long initTime;
    long actualTime;

    private boolean isPlaying;
    private boolean isGaming;
    private int playerSpeed;

    private Player player;
    private ArrayList<Bullet> playerBullets;
    private ArrayList<Cloud> clouds;


    private IceCreamCar icecreamCar;
    private ArrayList<Kid> kids;
    private ArrayList<Adult> adults;
    private ArrayList<PowerUp> powerUps;
    private Thread gameplayThread = null;
    private int score;
    private int lifes;
    private int nexTop;

    /**
     * Contructor
     *
     * @param context
     */
    public GameSurfaceView(Context context, float screenWidth, float screenHeight) {
        super(context);

        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        initTime = System.currentTimeMillis();
        frameCount = 0;

        player = new Player(context, screenWidth, screenHeight);
        playerSpeed = 7;
        playerBullets = new ArrayList<Bullet>();
        clouds = new ArrayList<Cloud>();


        icecreamCar = new IceCreamCar(context, screenWidth, screenHeight);
        kids = new ArrayList<Kid>();
        adults = new ArrayList<Adult>();
        powerUps = new ArrayList<PowerUp>();
        paint = new Paint();
        paint.setTextSize(screenWidth * 8 / 100);
        paint.setColor(Color.WHITE);
        holder = getHolder();
        isPlaying = true;
        isGaming = false;
        score = 0;
        lifes = 3;
        nexTop = 100;
    }

    /**
     * Method implemented from runnable interface
     */
    @Override
    public void run() {
        while (isPlaying) {
            frameCount++;
            frameCount %= Integer.MAX_VALUE - 10000;
            actualTime = System.currentTimeMillis();
            updateBackGround();
            if (actualTime - initTime > 500) {
                if (isGaming)
                    updateInfo();
                paintFrame();
            }

        }
    }

    private void updateBackGround() {
        if (random.nextInt(100) < 20 || (isGaming && random.nextInt(100) < 70))
            clouds.add(new Cloud(context, screenWidth, screenHeight, isGaming));
        for (Cloud c : clouds)
            c.updateInfo(isGaming);
        for (int i = 0; i < clouds.size(); i++)
            if (clouds.get(i).positionY() > screenHeight + clouds.get(i).spriteSizeHeigth())
                clouds.remove(i--);
    }

    private void updateInfo() {
        player.updateInfo(actualTime);

        if (frameCount % 120 == 0)
            playerBullets.add(new Bullet(context, screenWidth, screenHeight, player.positionX(), player.positionY(), true));
        if (frameCount % 120 == 60)
            playerBullets.add(new Bullet(context, screenWidth, screenHeight, player.positionX() + player.spriteSizeWidth() - (screenWidth * 2 / 1000 * 6), player.positionY(), true));
        for (Bullet pb : playerBullets)
            pb.updateInfo(actualTime);
        for (int i = 0; i < playerBullets.size(); i++) {
            if (playerBullets.get(i).positionY() < -playerBullets.get(i).spriteSizeHeigth())
                playerBullets.remove(i--);
        }


        if (random.nextInt(1000) < 1 && powerUps.isEmpty())
            powerUps.add(new PowerUp(context, screenWidth, screenHeight));
        for (PowerUp p : powerUps)
            p.updateInfo();
        for (int i = 0; i < powerUps.size(); i++)
            if (powerUps.get(i).getPositionX() < -powerUps.get(i).getSpriteSizeWidth())
                powerUps.remove(i--);
            else if (collitedWhithIceCreamCar(powerUps.get(i).getPositionX(), powerUps.get(i).getPositionY(), powerUps.get(i).getSpriteSizeWidth(), powerUps.get(i).getSpriteSizeHeigth())) {
                powerUps.remove(i--);
                for (Adult a : adults)
                    a.setPowerUp(true);
            }

        if (random.nextInt(100) < 2) {
            Kid toAdd = new Kid(context, screenWidth, screenHeight);
            kids.add(toAdd);
        }
        for (Kid k : kids)
            k.updateInfo();
        for (int i = 0; i < kids.size(); i++) {
            if (kids.get(i).getPositionX() < -kids.get(i).spriteSizeWidth) {
                kids.remove(i--);
                score--;
            } else if (collitedWhithIceCreamCar(kids.get(i).getPositionX(), kids.get(i).getPositionY(), kids.get(i).getSpriteSizeWidth(), kids.get(i).getSpriteSizeHeigth())) {
                kids.remove(i--);
                score += 2;
            }
        }

        if (random.nextInt(100) < 1)
            adults.add(new Adult(context, screenWidth, screenHeight));
        for (Adult a : adults)
            a.updateInfo();
        for (int i = 0; i < adults.size(); i++) {
            if (adults.get(i).getPositionX() < -adults.get(i).spriteSizeWidth)
                adults.remove(i--);
            else if (collitedWhithIceCreamCar(adults.get(i).getPositionX(), adults.get(i).getPositionY(), adults.get(i).spriteSizeWidth, adults.get(i).spriteSizeHeigth)) {
                if (adults.get(i).isPowerUp()) {
                    score++;
                } else {
                    lifes--;
                }
                adults.remove(i--);
            }
        }

        if (score >= nexTop) {
            lifes++;
            nexTop += 100;
        }

        icecreamCar.updateInfo();
    }

    private boolean collitedWhithIceCreamCar(float x, float y, int xlen, int ylen) {
        if (icecreamCar.getPositionX() > x + xlen || x > icecreamCar.getPositionX() + icecreamCar.SPRITE_SIZE_WIDTH) {
            return false;
        } else if (icecreamCar.getPositionY() > y + ylen || y > icecreamCar.getPositionY() + icecreamCar.SPRITE_SIZE_HEIGTH) {
            return false;
        }
        return true;
    }


    private void paintFrame() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            /*
            for (Kid k : kids) {
                canvas.drawBitmap(k.getSpriteKid(), k.getPositionX(), k.getPositionY(), paint);
            }
            for (Adult a : adults) {
                canvas.drawBitmap(a.getSpriteAdult(), a.getPositionX(), a.getPositionY(), paint);
            }
            for (PowerUp p : powerUps) {
                canvas.drawBitmap(p.getSpriteKid(), p.getPositionX(), p.getPositionY(), paint);
            }
            canvas.drawBitmap(icecreamCar.getSpriteIcecreamCar(), icecreamCar.getPositionX(), icecreamCar.getPositionY(), paint);
            */


            for (Cloud c : clouds)
                canvas.drawBitmap(c.spriteImage(), c.positionX(), c.positionY(), paint);
            for (Bullet pb : playerBullets)
                canvas.drawBitmap(pb.spriteImage(), pb.positionX(), pb.positionY(), paint);
            canvas.drawBitmap(player.spriteImage(), player.positionX(), player.positionY(), paint);
            canvas.drawText("Score: " + score, screenWidth / 100 * 10, screenWidth / 100 * 10, paint);
            canvas.drawText("Lifes: " + lifes, screenWidth / 100 * 50, screenWidth / 100 * 10, paint);
            holder.unlockCanvasAndPost(canvas);
        }

    }


    public void pause() {
        isPlaying = false;
        isGaming = false;
        try {
            gameplayThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void resume() {
        isPlaying = true;
        gameplayThread = new Thread(this);
        gameplayThread.start();
    }

    /**
     * Detect the action of the touch event
     *
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                isGaming = true;
                System.out.println("TOUCH UP - STOP JUMPING");
                icecreamCar.setJumping(false);
                break;
            case MotionEvent.ACTION_DOWN:
                System.out.println("TOUCH DOWN - JUMP");
                icecreamCar.setJumping(true);
                break;
        }

        isGaming = true;
        float xValue = motionEvent.getX();
        if (xValue <= screenWidth / 2) {
            player.setSpeed(-playerSpeed);
        } else
            player.setSpeed(playerSpeed);


        return true;
    }

}
