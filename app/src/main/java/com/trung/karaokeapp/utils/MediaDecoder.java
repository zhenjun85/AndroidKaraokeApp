package com.trung.karaokeapp.utils;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by quangthanh on 12/25/2017.
 */

public class MediaDecoder {
    private MediaExtractor extractor = new MediaExtractor();
    private MediaCodec decoder;

    private MediaFormat inputFormat;

    private ByteBuffer[] inputBuffers;
    private boolean end_of_input_file;
    private ByteBuffer[] processOutputBuffer;

    private ByteBuffer[] outputBuffers;
    private int outputBufferIndex = -1;
    private byte[] x = null;
    private byte[] y = null;

    public MediaDecoder(String inputFilename) throws IOException {
        extractor.setDataSource(inputFilename);

        // Select the first audio track we find.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                extractor.selectTrack(i);
                decoder = MediaCodec.createDecoderByType(mime);
                decoder.configure(format, null, null, 0);
                inputFormat = format;
                break;
            }
        }

        if (decoder == null) {
            throw new IllegalArgumentException("No decoder for file format");
        }

        decoder.start();
        inputBuffers = decoder.getInputBuffers();
        outputBuffers = decoder.getOutputBuffers();
        end_of_input_file = false;
    }

    // Read the raw data from MediaCodec.
    // The caller should copy the data out of the ByteBuffer before calling this again
    // or else it may get overwritten.
    @SuppressLint("WrongConstant")
    private byte[] readData(MediaCodec.BufferInfo info) {
        if (decoder == null)
            return null;

        for (;;) {
            // Read data from the file into the codec.
            if (!end_of_input_file) {
                int inputBufferIndex = decoder.dequeueInputBuffer(10000);
                if (inputBufferIndex >= 0) {
                    int size = extractor.readSampleData(inputBuffers[inputBufferIndex], 0);
                    if (size < 0) {
                        // End Of File
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        end_of_input_file = true;
                    } else {
                        decoder.queueInputBuffer(inputBufferIndex, 0, size, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }
            }

            // Read the output from the codec.
            if (outputBufferIndex >= 0)
                // Ensure that the data is placed at the start of the buffer
                outputBuffers[outputBufferIndex].position(0);

            outputBufferIndex = decoder.dequeueOutputBuffer(info, 10000);

            if (outputBufferIndex >= 0) {
                // Handle EOF
                if (info.flags != 0) {
                    System.out.println("/******** END ********/");
                    decoder.stop();
                    decoder.release();
                    decoder = null;
                    return x;
                }

                ByteBuffer t = outputBuffers[outputBufferIndex];

                if(x == null){
                    x = new byte[t.remaining()];
                    t.get(x);
                } else if(x != null) {
                    y = new byte[t.remaining()];
                    t.get(y);
                    byte[] z = x.clone();
                    x = new byte[y.length + z.length];
                    for(int i = 0; i < (y.length + z.length); i++) {
                        if(i < z.length) {
                            x[i] = z[i];
                        } else {
                            x[i] = y[i - z.length];
                        }
                    }
                }

                decoder.releaseOutputBuffer(outputBufferIndex, false);


            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // This usually happens once at the start of the file.
                outputBuffers = decoder.getOutputBuffers();
            }
        }
    }

    // Return the Audio sample rate, in samples/sec.
    public int getSampleRate() {
        return inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
    }

    // Read the raw audio data in 16-bit format
    // Returns null on EOF
    public float[] readShortData() {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        byte[] data = readData(info);

        if (data == null)
            return null;

        int samplesRead = data.length/2;
        float[] samples = new float[samplesRead];

        for (int i = 0, s = 0; i < samplesRead; ) {
            int sample = 0;

            sample |= data[i++] & 0xFF; // (reverse these two lines
            sample |= data[i++] << 8; // if the format is big endian)

            samples[s++] = sample / 32768.0f;
        }

        return samples;
    }
}
