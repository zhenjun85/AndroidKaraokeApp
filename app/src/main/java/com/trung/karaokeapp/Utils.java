package com.trung.karaokeapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.trung.karaokeapp.entities.ApiError;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.appclass.Line;
import com.trung.karaokeapp.appclass.LyricFile;
import com.trung.karaokeapp.appclass.Note;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class Utils {

    public static ApiError converErrors(ResponseBody response){
        Converter<ResponseBody, ApiError> converter = RetrofitBuilder.getRetrofit().responseBodyConverter(ApiError.class, new Annotation[0]);

        ApiError apiError = null;

        try {
            apiError = converter.convert(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiError;
    }

    //Util
    public static int convertBeatToMs(int beat, float bpm) {
        return Math.round(beat * 60000 / (bpm * 4));
    }
    public static Note parseLineToNote(String lineStr, int noteType, float bpm) {

        int pos2StartBeat, pos3Duration, pos4Pitch;
        String pos5text = "";

        String[] mStr = lineStr.split(" ");
        if (mStr.length < 5){
            return null;
        }

        pos2StartBeat = convertBeatToMs(Integer.parseInt(mStr[1]), bpm);
        pos3Duration = convertBeatToMs(Integer.parseInt(mStr[2]), bpm);
        pos4Pitch = Integer.parseInt(mStr[3]);

        int fromIndex = 0;
        for (int i = 0; i < 4; i++) {
            fromIndex = lineStr.indexOf(" ", (fromIndex == 0 ? 0 : (fromIndex + 1)));
        }
        pos5text = lineStr.substring(fromIndex + 1);

        return new Note( pos2StartBeat, pos3Duration, pos4Pitch, noteType , pos5text );
    }

    public static LyricFile openSongFile(String urlLyric) {
        LyricFile lyricFile = new LyricFile();


        try {
            FileInputStream fileInputStream = new FileInputStream(urlLyric);
            BufferedReader bR = new BufferedReader(new InputStreamReader(fileInputStream));

            //param for while loop
            String lineStr = bR.readLine();
            Note newNote = null;
            Line lineTmp = new Line();

            //region whileLoop1
            while (lineStr != null) {
                char cFirst;
                switch ( cFirst = lineStr.charAt(0)) {
                    case '#':
                        lineStr = lineStr.substring(1); //remove char 0
                        String[] info = lineStr.split(":");
                        if (info.length < 2) {
                            break;
                        }
                        //region infoSwitch
                        switch (info[0]){
                            case "ARTIST":
                                lyricFile.artist = info[1];
                                break;
                            case "TITLE":
                                lyricFile.title = info[1];
                                break;
                            case "MP3":
                                lyricFile.mp3 = info[1];
                                break;
                            case "BPM":
                                lyricFile.bpm = Float.parseFloat(info[1].replace(',', '.'));
                                break;
                            case "GAP":
                                lyricFile.gap = Integer.parseInt(info[1]);
                                break;
                            case "GENRE":
                                lyricFile.genre = info[1];
                                break;
                            case "LANGUAGE":
                                lyricFile.language = info[1];
                                break;
                            case "COVER":
                                lyricFile.cover = info[1];
                                break;
                            case "BACKGROUND":
                                lyricFile.background = info[1];
                                break;
                            case "ENCODING":
                                lyricFile.encoding = info[1];
                                break;
                            case "DATE":
                                lyricFile.date = info[1];
                                break;
                            case "AUTHOR":
                                lyricFile.author = info.length > 1 ? info[1] : "";
                                break;
                            default:break;
                        }
                        //endregion
                        break;
                    case ':':
                        newNote = parseLineToNote(lineStr, 1, lyricFile.bpm);
                        lineTmp.notes.add(newNote);
                        break;
                    case 'E':
                        String s2 = "";
                        Note notePrevious = lineTmp.notes.get(lineTmp.notes.size() - 1);
                        lineTmp.end = notePrevious.start + notePrevious.length;
                        lineTmp.start =  lineTmp.notes.get(0).start;

                        //get line lyric
                        for (Note iNote : lineTmp.notes) {
                            s2 += iNote.text;
                        }
                        lineTmp.lyric = s2;
                        lyricFile.allLines.add(lineTmp);
                        break;

                    case '-': //end line
                        String[] mStr = lineStr.split(" ");
                        String  s = "";

                        lineTmp.end =  convertBeatToMs(Integer.parseInt(mStr[1]), lyricFile.bpm);
                        lineTmp.start =  lineTmp.notes.get(0).start;

                        //get line lyric
                        for (Note note: lineTmp.notes) {
                            s += note.text;
                        }

                        lineTmp.lyric = s;
                        lyricFile.allLines.add(lineTmp);

                        //new lineTmp prepare for next line
                        lineTmp = new Line();
                        break;
                    case '*':
                        newNote = parseLineToNote(lineStr, 2, lyricFile.bpm);
                        lineTmp.notes.add(newNote);
                        break;
                    case 'F':
                        newNote = parseLineToNote(lineStr, 0, lyricFile.bpm);
                        lineTmp.notes.add(newNote);
                        break;
                    default:break;
                }
                lineStr = bR.readLine();
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lyricFile;
    }

    public static String convertTimeMsToMMSS(int currentTime){
        long minutes = TimeUnit.MILLISECONDS.toMinutes((long) currentTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%s:%s", minutes < 10 ? "0" + minutes : "" + minutes, (seconds < 10) ? "0" + seconds : "" + seconds);
    }
    public static int convertPitchToScore(float pitch, int sampleNote) {
        pitch = pitch < 0 ? 0 : pitch;
        int n = (int)Math.round(Math.log(pitch / 440)/Math.log(2) * 12);
        int score = 10 - Math.abs(Math.abs(9-n) - Math.abs(9-sampleNote));
        if (score <= 10 && score >= 0)
            return score;
        return 0;
    }

}
