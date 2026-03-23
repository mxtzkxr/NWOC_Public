package ru.nwoc.T_Invest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nwoc.T_Invest.service.InfoService;
import ru.nwoc.T_Invest.service.MessageSenderImpl;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InfoController {
    private final InfoService infoService;
    private final MessageSenderImpl messageSender;

    @GetMapping("/info")
    public void getInfo() {
        infoService.getSchedule();
        infoService.getUserInfo();
        infoService.getAccounts();
    }

    @PostMapping("/try-rabbit")
    public ResponseEntity<?> sendMessage(@RequestBody String text){
        if( text.isBlank() ){
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        //messageSender.sendMessage(text);
        infoService.getInfo(text);
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }


}
