package jp.co.seattle.library.controller;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.seattle.library.service.BooksService;
import jp.co.seattle.library.service.LendingService;

/**
 * 詳細表示コントローラー
 */
@Controller
public class DetailsController {
    final static Logger logger = LoggerFactory.getLogger(BooksService.class);
    @Autowired
    private BooksService booksService;

    @Autowired
    private LendingService lendingService;
    /**
     * 詳細画面に遷移する
     * @param locale
     * @param bookId
     * @param model
     * @return
     */
    @Transactional
    @RequestMapping(value = "/details", method = RequestMethod.POST)
    public String detailsBook(Locale locale,
            @RequestParam("bookId") Integer bookId,
            Model model) {
        // デバッグ用ログ
        logger.info("Welcome detailsBook.java! The client locale is {}.", locale);

        model.addAttribute("bookDetailsInfo", booksService.getBookInfo(bookId));

        //貸出状況を確認とフロントに表示
        int count = lendingService.LendingConfirmation(bookId);
        if (count == 1) {
            //借りるボタン_非活性
            model.addAttribute("rentActivation", "disabled");
            //返すボタン_活性
            model.addAttribute("returnActivation");
            //貸出ステータス
            model.addAttribute("lendingStatus", "貸出中");
        } else {
            //借りるボタン_活性
            model.addAttribute("rentActivation");
            //返すボタン_非活性
            model.addAttribute("returnActivation", "disabled");
            //貸出ステータス
            model.addAttribute("lendingStatus", "貸出可能");
        }


        return "details";
    }
}
