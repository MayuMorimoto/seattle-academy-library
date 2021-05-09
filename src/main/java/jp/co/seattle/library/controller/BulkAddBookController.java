package jp.co.seattle.library.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;

/**
 * @author morimotomayu
 *
 */
@Controller //APIの入り口
public class BulkAddBookController {
    final static Logger logger = LoggerFactory.getLogger(AddBooksController.class);
    final static String EXTENSION = ".csv";
    
    @Autowired
    BooksService booksService;
    /**
     * 一括登録画面初期表示
     * @param model モデル
     * @return 一括登録画面
     */
    @RequestMapping(value = "/bulkAddBook", method = RequestMethod.GET)
    public String showBulkAdd(Model model) {
    	return "bulkRegist";
    }
    
    /**
     * 一括登録ボタン押下時処理
     * @param locale ロケール情報
     * @param file csvファイル
     * @param model モデル
     * @return Home画面
     */
    @Transactional
    @RequestMapping(value = "/bulkRegist", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public String bulkRegist(Locale locale,
			@RequestParam("csv") MultipartFile file,
			Model model) {
        logger.info("Welcome insertBooks.java! The client locale is {}.", locale);
        //ファイル名取得
        String fileName = file.getOriginalFilename();
        //CSVファイル形式であることのチェック
        checkCsvFile(fileName,model);
        
        //ファイルを読み込む
        BufferedReader br = null;
    	//読み込んだ配列を格納するList
    	List<String[]> contentsList = new ArrayList<String[]>();
        try {
        	InputStream stream = file.getInputStream(); 
        	Reader reader = new InputStreamReader(stream);
        	br = new BufferedReader(reader);
        	//読み込み行
        	String line;
        	//分割後のデータを保持する配列
        	String[] dataList;
        	//2行目以降1行ずつ読み込み
        	while((line = br.readLine()) != null) {
        		//1行の中で各項目をカンマで分割し、Listに追加
        		dataList = line.split(",");
        		//booksTBLのカラム数と配列のサイズが同じであるか確認
        		if(!checkColumnSize(dataList)) {
                  	model.addAttribute("error","必須項目が足りません。");
                	return "bulkRegist";
        		};
        		//各項目のバリデーションチェック
        		//出版日のバリデーションチェック
        		if(!checkPublishDate(dataList)) {
                  	model.addAttribute("error","正しい出版日を入力してください");
                	return "bulkRegist";
        		}
        		//ISBNのバリデーションチェック
        		if(!checkIsbn(dataList)) {
                  	model.addAttribute("error","ISBNは10桁もしくは13桁の数字で入力してください。");
                	return "bulkRegist";
        		}
        		contentsList.add(dataList);
        	}
        }catch(Exception ex) {
        	ex.printStackTrace();
        	model.addAttribute("error","ファイルの読み込みに失敗しました");
        	return "bulkRegist";
        }finally {
            try{
                br.close();
              } catch(Exception ex) {
              	model.addAttribute("error","ファイルの読み込みに失敗しました");
            	return "bulkRegist";
              }
        }
		//DB登録      		
		setBookDetailsInfo(contentsList);
        
        model.addAttribute("bookList", booksService.getBookList());
    	return "home";
    }
    
    
    /**
     * CSVファイル形式チェック
     * @param file 画面で設定されたファイル情報
     * @param model モデル
     * @return 一括登録画面 or null
     */
    public String checkCsvFile(String fileName, Model model) {
        //拡張子の部分を取得
        int index = fileName.indexOf(".");
        index += ".".length();
        String extension = fileName.substring(index);
        //拡張子部分が.csvではない場合、エラーメッセージ表示
        if(!extension.equals(EXTENSION)) {
        	model.addAttribute("error","CSVファイルを選択してください");
        	return "bulkRegist";
        }
        return null;
    }
    
    
    /**
     * CSVファイルのカラム数チェック
     * @param dataList 1行のデータ
     * @return チェックフラグ
     */
    private boolean checkColumnSize(String[] dataList) {
    	//Listサイズが4以上5以下であることの確認（ISBNは任意の項目のため）
    	if(!(dataList.length >=4 && dataList.length<= 5)) {
    		return false;
    	}
    	//配列0番目〜3番目の値がnullではないことの確認
    	for(int i = 0; i<=3; i++) {
    		if(dataList[i] == null) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * 出版日のバリデーションチェック
     * @param dataList 1行のデータ
     * @return チェックフラグ
     */
    private boolean checkPublishDate(String[] dataList) {
    	//出版日のバリデーションチェック
    	if (!(dataList[3].matches("[0-9]{8}"))) {
            return false;
        } else {
            try {
                DateFormat df = new SimpleDateFormat("yyyyMMdd");
                df.setLenient(false);
                df.parse(dataList[3]);
            } catch (ParseException p) {
            	return false;
            }
        }
    	return true;
    }
    
    /**
     * ISBNのバリデーションチェック
     * @param dataList 1行のデータ
     * @return チェックフラグ
     */
    private boolean checkIsbn(String[] dataList) {
    	//ISBNのバリデーションチェック
    	if(!dataList[4].matches("([0-9]{10}|[0-9]{13})?")) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * DB登録
     * @param contentsList 全行のデータ
     */
    public void setBookDetailsInfo (List<String[]> contentsList) {
    	for(String[]bookDetailsInfoList : contentsList) {
    		//BookDetailsInfoに格納
        	BookDetailsInfo bookDetailsInfo = new BookDetailsInfo();
        	//書籍名
        	bookDetailsInfo.setTitle(bookDetailsInfoList[0]);
        	//著者名
        	bookDetailsInfo.setAuthor(bookDetailsInfoList[1]);
        	//出版社
        	bookDetailsInfo.setPublisher(bookDetailsInfoList[2]);
        	//出版日
        	bookDetailsInfo.setPublishDate(bookDetailsInfoList[3]);
        	//ISBN
        	bookDetailsInfo.setIsbn(bookDetailsInfoList[4]);
        	//DBに登録
        	booksService.registBook(bookDetailsInfo);
    	}
    	
    }
}
