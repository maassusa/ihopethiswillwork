package com.wiredtext;

import java.util.Random;

public class WiredTextState {

    public static boolean active = false;
    public static boolean overdrive = false;
    public static long minDelayMs = 60_000L;
    public static long maxDelayMs = 360_000L;
    public static long nextTriggerTime = 0;
    public static long showUntil = 0;
    public static String currentText = "";
    public static float textX = 0.5f;
    public static float textY = 0.5f;

    public static final long DISPLAY_DURATION_MS = 400;
    public static final long OVERDRIVE_DISPLAY_MS = 1000;

    private static final Random RANDOM = new Random();

    private static final String[] PHRASES = {
        "что я тут делаю", "где я", "я сплю?", "это не настоящее",
        "я уже здесь был", "выйди отсюда", "ты не один", "смотри за собой",
        "оглянись", "он за тобой", "я тебя вижу", "ты забыл что-то важное",
        "это всё сон", "просыпайся", "ты умер во сне", "это не твой мир",
        "кто ты такой", "я тебя знаю", "беги", "не смотри вниз",
        "тебя здесь нет", "это не твои руки", "не открывай глаза",
        "ты давно потерялся", "это уже было", "помни", "забудь",
        "не возвращайся", "здесь никого нет", "ты один", "они уже ушли",
        "где твой дом", "ты не помнишь меня", "я был здесь первым",
        "не оглядывайся", "он знает", "тихо", "осторожно", "не дыши"
    };

    private static final String CHARS = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзиклмнопрстуфхцчшщъыьэюя";

    public static String randomGibberish() {
        int len = 4 + RANDOM.nextInt(10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (RANDOM.nextFloat() < 0.15f) sb.append(' ');
            else sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString().trim();
    }

    public static String pickText() {
        if (RANDOM.nextFloat() < 0.40f) return randomGibberish();
        return PHRASES[RANDOM.nextInt(PHRASES.length)];
    }

    public static void scheduleNext() {
        long delay = overdrive ? 300L :
            minDelayMs + (long)(RANDOM.nextDouble() * (maxDelayMs - minDelayMs));
        nextTriggerTime = System.currentTimeMillis() + delay;
    }

    public static void trigger() {
        currentText = pickText();
        textX = 0.05f + RANDOM.nextFloat() * 0.85f;
        textY = 0.05f + RANDOM.nextFloat() * 0.85f;
        showUntil = System.currentTimeMillis() + (overdrive ? OVERDRIVE_DISPLAY_MS : DISPLAY_DURATION_MS);
        scheduleNext();
    }

    public static boolean isShowing() {
        return active && System.currentTimeMillis() < showUntil;
    }

    public static void tick() {
        if (!active) return;
        if (nextTriggerTime > 0 && System.currentTimeMillis() >= nextTriggerTime) {
            trigger();
        }
    }
}
