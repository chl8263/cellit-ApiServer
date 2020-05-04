package me.ewan.cellit.mainBoard

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class MainBoardController {

    @GetMapping
    fun mainBoardForm() : String = "/mainBoard"
}