package jp.co.seattle.library.controller;

import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.seattle.library.dto.UserInfo;
import jp.co.seattle.library.service.BooksService;
import jp.co.seattle.library.service.UsersService;

/**
 * アカウント作成コントローラー
 */
@Controller //APIの入り口
public class AccountController {
    final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private BooksService booksService;
    @Autowired
    private UsersService usersService;

    @RequestMapping(value = "/newAccount", method = RequestMethod.GET) //value＝actionで指定したパラメータ
    public String createAccount(Model model) {
        return "createAccount";
    }

    /**
     * 新規アカウント作成
     *
     * @param email メールアドレス
     * @param password パスワード
     * @param passwordForCheck 確認用パスワード
     * @param model
     * @return　ホーム画面に遷移
     */
    @Transactional
    @RequestMapping(value = "/createAccount", method = RequestMethod.POST)
    public String createAccount(Locale locale,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("passwordForCheck") String passwordForCheck,
            Model model) {
        // デバッグ用ログ
        logger.info("Welcome createAccount! The client locale is {}.", locale);

        // パラメータで受け取った書籍情報をDtoに格納する。
        UserInfo userInfo = new UserInfo();
        
        // email形式の正規表現
        String emailValidation = "^[\\w!#%&'/=~`\\*\\+\\?\\{\\}\\^\\$\\-\\|]+(\\.[\\w!#%&'/=~`\\*\\+\\?\\{\\}\\^\\$\\-\\|]+)*@[\\w!#%&'/=~`\\*\\+\\?\\{\\}\\^\\$\\-\\|]+(\\.[\\w!#%&'/=~`\\*\\+\\?\\{\\}\\^\\$\\-\\|]+)*$";
        Pattern emailPattern = Pattern.compile(emailValidation); 
        // emailバリデーションチェック実施
        boolean emailValidationCheckFlg = emailPattern.matcher(email).find();
        if(!emailValidationCheckFlg) {
        	//バリデーションチェックNG（false）の場合、エラーメッセージを画面に返す
        	model.addAttribute("emailValidationMessage", "email形式で入力してください。");
        	return "createAccount";
        }
        //メール形式チェックがOKの場合、userInfoにsetする
        userInfo.setEmail(email);
        
        //半角英数字の正規表現
        String alphanumericValidation = "^[0-9a-zA-Z]+$";
        Pattern alphanumericPattern = Pattern.compile(alphanumericValidation);
        //パスワード半角英数字バリデーションチェック実施
        boolean passwordAlphanumericCheckFlg = alphanumericPattern.matcher(password).find();
        //確認パスワード半角英数字バリデーションチェック実施
        boolean confirmedPasswordAlphanumericCheckFlg = alphanumericPattern.matcher(passwordForCheck).find();
        if(!passwordAlphanumericCheckFlg || !confirmedPasswordAlphanumericCheckFlg) {
        	//パスワードまたは、確認用パスワードどちらかがバリデーションチェックNG（false）の場合、エラーメッセージを画面に返す
        	model.addAttribute("passwordValidationMessage", "半角英数字で入力してください。");
        	return "createAccount";
        }      
        
        //パスワード一致チェック
        //パスワードと確認用パスワードの値が異なる場合、エラーメッセージを画面に返す
        if(!password.equals(passwordForCheck)) {
        	model.addAttribute("passwordCheck", "パスワードが一致しません。");
        	return "createAccount";
        }
        //パスワードの半角英数字チェック、確認用パスワードチェックがOKの場合、userInfoにパスワードをsetする
        userInfo.setPassword(password);
        //serviceクラスにうせrInfoを渡す
        usersService.registUser(userInfo);
        
        //BooksServiceクラスで取得した本の一覧を画面に返す
        model.addAttribute("bookList", booksService.getBookList());
        return "home";
    }

}
