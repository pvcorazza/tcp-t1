package bank.business.domain;

import java.util.ArrayList;
import java.util.List;

import bank.business.BusinessException;

/**
 * @author Ingrid Nunes
 * 
 */
public class CurrentAccount implements Credentials {

	private double balance;
	private Client client;
	private List<Deposit> deposits;
	private CurrentAccountId id;
	private List<Transfer> transfers;
	private List<Withdrawal> withdrawals;
	private final double ITF = 0.002; //0.2%
	
	//save the deposit index that was not confirmed by employee
//	private List<Integer> pendingIndexDeposits;

	public CurrentAccount(Branch branch, long number, Client client) {
		this.id = new CurrentAccountId(branch, number);
		branch.addAccount(this);
		this.client = client;
		client.setAccount(this);
		this.deposits = new ArrayList<>();
		this.transfers = new ArrayList<>();
		this.withdrawals = new ArrayList<>();
	}

	public CurrentAccount(Branch branch, long number, Client client,
			double initialBalance) {
		this(branch, number, client);
		this.balance = initialBalance;
	}

	/**
	 * 
	 * @param location
	 * @param envelope
	 * @param amount
	 * @return
	 * @throws BusinessException
	 */
	public Deposit deposit(OperationLocation location, long envelope,
			double amount) throws BusinessException {
		
		
		depositAmount(amount);

		Deposit deposit = new Deposit(location, this, envelope, amount);
		this.deposits.add(deposit);
		
		
		
		switch (deposit.getStatus()) {
			
			case CONFIRMED:
				//only confirmed deposit values are sum 
				this.balance += amount;
				break;
			case PENDING:
				// save the index of the deposit (last deposit)
				// nothing to sum to the balance account because deposit status == PENDING
				//this.pendingIndexDeposits.add(deposits.size()-1);
				
				break;
				
			default:
				break;
		}
		
		return deposit;
	}

	private void depositAmount(double amount) throws BusinessException {
		if (!isValidAmount(amount)) {
			throw new BusinessException("exception.invalid.amount");
		}

		//this.balance += amount;
	}
	
	private void depositTranferAmount(double amount) throws BusinessException {
		if (!isValidAmount(amount)) {
			throw new BusinessException("exception.invalid.amount");
		}

		this.balance += amount;
	}
	
	public void sumAmount(double amount){
		this.balance += amount;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @return the deposits
	 
	public List<Deposit> getDeposits() {
		return deposits;
	}

	/**
	 * @return the id
	 */
	public CurrentAccountId getId() {
		return id;
	}

	public List<Transaction> getTransactions() {
		List<Transaction> transactions = new ArrayList<>(deposits.size()
				+ withdrawals.size() + transfers.size());
		transactions.addAll(deposits);
		transactions.addAll(withdrawals);
		transactions.addAll(transfers);
		return transactions;
	}

	/**
	 * @return the transfers
	 */
	public List<Transfer> getTransfers() {
		return transfers;
	}

	/**
	 * @return the withdrawals
	 */
	public List<Withdrawal> getWithdrawals() {
		return withdrawals;
	}
	
	private boolean hasEnoughBalance(double amount) {
		return amount <= balance;
	}

	private boolean isValidAmount(double amount) {
		return amount > 0;
	}

	public Transfer transfer(OperationLocation location,
			CurrentAccount destinationAccount, double amount)
			throws BusinessException {
		
		double amountWithItf = amount+amount*ITF;
		
		withdrawalAmount(amountWithItf);
		destinationAccount.depositTranferAmount(amount);

		Transfer transfer = new Transfer(location, this, destinationAccount,
				amount);
		transfer.setAmountItf(amount*ITF);
		this.transfers.add(transfer);
		destinationAccount.transfers.add(transfer);

		return transfer;
	}

	public Withdrawal withdrawal(OperationLocation location, double amount)
			throws BusinessException {
		double amountWithItf = amount+amount*ITF;
		withdrawalAmount(amountWithItf);

		Withdrawal withdrawal = new Withdrawal(location, this, amount);
		this.withdrawals.add(withdrawal);
		withdrawal.setAmountItf(ITF*amount);

		return withdrawal;
	}

	private void withdrawalAmount(double amount) throws BusinessException {
		if (!isValidAmount(amount)) {
			throw new BusinessException("exception.invalid.amount");
		}

		if (!hasEnoughBalance(amount)) {
			throw new BusinessException("exception.insufficient.balance");
		}

		this.balance -= amount;
	}
	
//	public void confirmDeposit(Deposit deposit){
//		int index = deposits.indexOf(deposit);
//		deposits.get(index).setStatusConfirmed();
//	}

}
