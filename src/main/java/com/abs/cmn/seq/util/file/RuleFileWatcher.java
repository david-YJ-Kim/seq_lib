package com.abs.cmn.seq.util.file;

import com.abs.cmn.seq.SequenceManager;
import com.abs.cmn.seq.util.SequenceManageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;

public class RuleFileWatcher implements Runnable{

    public static Logger logger = LoggerFactory.getLogger(RuleFileWatcher.class);

    private final String filePath;
    private final String fileName;

    private final SequenceManager sequenceManager;

    public RuleFileWatcher(SequenceManager sequenceManager, String filePath, String fileName){
        this.sequenceManager = sequenceManager;
        this.fileName = fileName;
        this.filePath = filePath;
    }

//    public RuleFileWatcher(String filePath, String fileName){
//        this.fileName = fileName;
//        this.filePath = filePath;
//    }

    @Override
    public void run() {

        logger.info("File Watch Service is now Start w/ filePath: {}, fileName: {}", this.filePath, this.fileName);
        try{
            this.startWatchService();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void startWatchService() throws IOException, InterruptedException {

        Path directory = Paths.get(this.filePath);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    Path file = (Path) event.context();

                    // File 이름 조회
                    if(fileName.equals(file.getFileName().toString())){
                        String fileContent = SequenceManageUtil.readFile(this.filePath.concat(fileName));
                        logger.info("{} file has been modified. its file Name: {}, file Content: {}",
                                System.currentTimeMillis(), file.getFileName(), fileContent);
                        this.sequenceManager.fileChangeDetecting(file.getFileName().toString(), fileContent);
                    }else{
                        logger.warn("This is not target file. fileName:{}", file.getFileName().toString());
                    }

                }else{
                    System.err.println(String.valueOf(System.currentTimeMillis()) + "Its un predicted event. event:" + event.toString());
                }
            }
            key.reset();
        }
    }


    @Deprecated
    public void sampleCodeForOtherEvents() throws IOException, InterruptedException {

        String testFilepath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\test\\filechange\\";
        String fileName = "changetest.txt";

        Path directory = Paths.get(testFilepath);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);


        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    Path file = (Path) event.context();
                    System.out.println(String.valueOf(System.currentTimeMillis()) + " 파일이 수정되었습니다: " + file.getFileName());
                    try{
                        System.out.println(
                                SequenceManageUtil.readFile(testFilepath + file.getFileName())
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }

                } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path file = (Path) event.context();
                    System.out.println(String.valueOf(System.currentTimeMillis()) + " 새 파일이 생성되었습니다: " + file.getFileName());
                    try{
                        System.out.println(
                                SequenceManageUtil.readFile(testFilepath + file.getFileName())
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }

                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    Path file = (Path) event.context();
                    System.out.println(String.valueOf(System.currentTimeMillis()) + " 파일이 삭제되었습니다: " + file.getFileName());
                }
            }
            key.reset();
        }
    }

    public static void main(String[] args) throws Exception {

        String testFilepath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\test\\filechange\\";
        String fileName = "changetest.txt";

        int numThread = 50;
        CountDownLatch startSignal = new CountDownLatch(1); // Start signal
        CountDownLatch doneSignal = new CountDownLatch(numThread); // Done signal

        for (int i = 0; i < numThread; i ++){
            Thread watcher = new Thread(new RuleFileWatcher(null, testFilepath, fileName));
            watcher.start();
        }

        startSignal.countDown();
        doneSignal.await();
        System.out.println("All threads have finished.");

    }
}
