package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.core.tables.UserService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.List;
import java.util.concurrent.Future;

@DependsOn({"threadPoolTaskExecutor", "devVars", "tableManagerService", "debuggingService"})
@Service
@ApplicationScope
public class TransactionThreadService {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private DevVars devVars;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private UserService userService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private DebuggingService debuggingService;

    public Transaction createRows(Transaction transaction) throws Exception{
        return CreateRows(transaction);
    }

    public Transaction createRowsAdvanced(Transaction transaction) throws Exception{
        return CreateRowsAdvanced(transaction);
    }

    public Transaction createTable(Transaction transaction) throws Exception{
        return CreateTable(transaction);
    }

    public Transaction logOff(Transaction transaction, Session session) throws Exception{
        return LogOff(transaction, session);
    }

    public Transaction logOn(Transaction transaction, Session session) throws Exception{
        return LogOn(transaction, session);
    }

    public Transaction selectAdvanced(Transaction transaction, List<String> tableNames) throws Exception{
        return SelectAdvanced(transaction, tableNames);
    }

    public Transaction select(Transaction transaction) throws Exception{
        return Select(transaction);
    }

    public Transaction putAdvanced(Transaction transaction) throws Exception{
        return PutAdvanced(transaction);
    }

    public Transaction put(Transaction transaction) throws Exception{
        return Put(transaction);
    }

    public Transaction deleteAdvanced(Transaction transaction) throws Exception{
        return DeleteAdvanced(transaction);
    }

    public Transaction deleteTable(Transaction transaction) throws Exception{
        return DeleteTable(transaction);
    }

    public Transaction delete(Transaction transaction) throws Exception{
        return Delete(transaction);
    }

    private Transaction CreateRows(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new CreateRows(transaction, tableManagerService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction CreateRowsAdvanced(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new CreateRowsAdvanced(transaction, tableManagerService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction CreateTable(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new CreateTable(transaction, tableManagerService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction LogOff(Transaction transaction, Session session) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new LogOff(transaction, session, sessionService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction LogOn(Transaction transaction, Session session) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new LogOn(transaction, session, userService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction SelectAdvanced(Transaction transaction, List<String> tableNames) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new SelectAdvanced(transaction, tableNames, tableManagerService, devVars));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction Select(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new Select(transaction, this));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction PutAdvanced(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new PutAdvanced(transaction, tableManagerService, devVars));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction Put(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new Put(transaction, this));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction DeleteAdvanced(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new DeleteAdvanced(transaction, tableManagerService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction DeleteTable(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new DeleteTable(transaction, tableManagerService, userService));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

    private Transaction Delete(Transaction transaction) throws Exception{
        Future<Transaction> future = threadPoolTaskExecutor.submit(new Delete(transaction, this));
        try{
            return future.get();
        }catch (Exception ex){
            throw ex;
        }
    }

}