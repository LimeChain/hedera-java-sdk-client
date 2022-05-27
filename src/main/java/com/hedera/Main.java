package com.hedera;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.utils.AccountWrapper;
import com.hedera.utils.ContractCreator;

import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static final long ONE_HBAR = 100_000_000L;
    public static final long DEFAULT_AMOUNT_TO_SEND = 20 * ONE_HBAR;

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException, ExecutionException {
        final var contractName = "TokenCreateContract";
        AccountWrapper accountWrapper = AccountWrapper.forLocalNode();
        final var node = accountWrapper.node();
        AccountId myAccountId = accountWrapper.getAccountId();
        PrivateKey myPrivateKey = accountWrapper.getPrivateKey();
        PublicKey myPublicKey = accountWrapper.getPublicKey();
        Client client = Client.forNetwork(node)
                .setOperator(new AccountId(2), myPrivateKey);

//Creating a contract by its binary, the name of the file and contractName must match
        var contractId = new ContractCreator().create(contractName, client, accountWrapper);
        ContractExecuteTransaction tx = new ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(500000)
                .setMaxTransactionFee(Hbar.from(8000))
                .setPayableAmount(Hbar.from(10))
              .setTransactionValidDuration(Duration.ofSeconds(170))
                .setFunction("createFungible", new ContractFunctionParameters()
                        .addBytes(myPublicKey.toBytes())
                        .addAddress(myAccountId.toSolidityAddress())
                        .addUint32(8000000));

        TransactionResponse submitTransfer = tx.execute(client);
        System.out.println(submitTransfer.transactionId);
        System.out.println(submitTransfer.getReceipt(client).toString());
//        dummyTestQueryExample(contractId,client);
    }

    private static void dummyTestQueryExample(ContractId contractId, Client client) throws PrecheckStatusException, TimeoutException {
        ContractExecuteTransaction tx = new ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(500000)
                .setMaxTransactionFee(Hbar.from(8000))
                .setPayableAmount(Hbar.from(10))
                .setFunction("setX", new ContractFunctionParameters()
                        .addUint256(BigInteger.TEN));

        TransactionResponse submitTransfer = tx.execute(client);
        System.out.println(submitTransfer.transactionId);

        ContractCallQuery query1 = new ContractCallQuery()
                .setContractId(new ContractId(1187))
                .setFunction("x")
                .setGas(2312312);
        System.out.println(query1.execute(client).getUint256(0));
    }
}