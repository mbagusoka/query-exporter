package com.personal.queryexporter.util;

import com.personal.queryexporter.common.constant.ExporterConstants;
import com.personal.queryexporter.common.service.ZipService;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class ZipServiceImpl implements ZipService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipServiceImpl.class);
    private static final String ZIP = ".zip";

    @SuppressWarnings("unchecked")
    @Override
    public void zip(Map<String, Object> params) {
        LOGGER.info("------START ZIPPING FILES------");
        List<File> files = (List<File>) params.get(ExporterConstants.FILES);
        try (ZipOutputStream outputStream = new ZipOutputStream(
                new FileOutputStream(
                        new File(params.get(ExporterConstants.FILE_PATH).toString().concat(ZIP))))) {
            ZipParameters zipParameters = this.getParams(params);
            this.zipFiles(outputStream, files, zipParameters);
        } catch (ZipException | IOException e) {
            LOGGER.error("ZipServiceImpl Exception: ", e);
        }
        this.deleteFiles(files);
        LOGGER.info("------END ZIPPING FILES------");
    }

    private ZipParameters getParams(Map<String, Object> params) {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (params.containsKey(ExporterConstants.PASS_KEY)) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParameters.setPassword(params.get(ExporterConstants.PASS_KEY).toString());
        }
        return zipParameters;
    }

    private void zipFiles(ZipOutputStream outputStream, List<File> files, ZipParameters zipParameters) throws ZipException, IOException {
        for (File file : files) {
            try (InputStream inputStream = new FileInputStream(file)) {
                outputStream.putNextEntry(file, zipParameters);
                byte[] readBuff = new byte[4096];
                int readLen;
                while ((readLen = inputStream.read(readBuff)) != -1){
                    outputStream.write(readBuff, 0, readLen);
                }
                outputStream.closeEntry();
            }
        }
        outputStream.finish();
    }

    private void deleteFiles(List<File> files) {
        Path path;
        try {
            for (File file : files) {
                path = Paths.get(file.getPath());
                Files.delete(path);
            }
        } catch (IOException e) {
            LOGGER.error("Delete Files Exception: ", e);
        }
    }
}
