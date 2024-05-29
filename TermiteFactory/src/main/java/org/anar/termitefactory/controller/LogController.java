package org.anar.termitefactory.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RestController
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmController.class);

    @RequestMapping(value = "/log/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        //ローカルファイルの読み込み
        try (InputStream is = new FileInputStream("./logs/spring.log");
             OutputStream os = response.getOutputStream();) {
            //レスポンスヘッダーの設定
            response.setHeader("Content-Disposition", "attachment; filename=spring.log");
            response.setContentType("application/octet-stream");
            //レスポンスボディへの書き込み
            IOUtils.copy(is, os);
            os.flush();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
