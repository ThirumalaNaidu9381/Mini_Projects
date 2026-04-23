const API_URL = 'http://localhost:8080/api';
let currentUser = null;
let currentAccounts = [];
let selectedAccount = null;

// TODO: maybe change this to an env variable later if we deploy

// DOM Elements
const authSection = document.getElementById('auth-section');
const dashboardSection = document.getElementById('dashboard-section');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const tabLogin = document.getElementById('tab-login');
const tabRegister = document.getElementById('tab-register');
const userDisplay = document.getElementById('user-display');
const accountsList = document.getElementById('accounts-list');

// Auth Tabs
tabLogin.addEventListener('click', () => {
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    loginForm.classList.add('active');
    registerForm.classList.remove('active');
});

tabRegister.addEventListener('click', () => {
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    registerForm.classList.add('active');
    loginForm.classList.remove('active');
});

// Auth Submit
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const res = await fetch(`${API_URL}/login`, {
            method: 'POST',
            body: JSON.stringify({username, password})
        });
        const data = await res.json();
        
        if (res.ok) {
            currentUser = data.username;
            console.log(`User ${currentUser} logged in successfully`);
            showDashboard();
        } 
        else {
            console.error("Login failed:", data.message);
            document.getElementById('login-error').innerText = data.message || 'Login failed';
        }
    } 
    catch (err) {
        console.error("Network error during login:", err);
        document.getElementById('login-error').innerText = 'Server error';
    }
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    
    try {
        const res = await fetch(`${API_URL}/register`, {
            method: 'POST',
            body: JSON.stringify({username, password})
        });
        const data = await res.json();
        
        if (res.ok) {
            document.getElementById('reg-error').style.color = 'var(--success)';
            document.getElementById('reg-error').innerText = 'Registration successful! Please log in.';
            setTimeout(() => tabLogin.click(), 1500);
        } 
        else {
            document.getElementById('reg-error').style.color = 'var(--error)';
            document.getElementById('reg-error').innerText = data.message || 'Registration failed';
        }
    } 
    catch (err) {
        document.getElementById('reg-error').innerText = 'Server error';
    }
});

document.getElementById('logout-btn').addEventListener('click', () => {
    currentUser = null;
    selectedAccount = null;
    authSection.classList.add('active');
    dashboardSection.classList.remove('active');
    document.getElementById('login-password').value = '';
});

// Dashboard Logic
async function showDashboard() {
    authSection.classList.remove('active');
    dashboardSection.classList.add('active');
    userDisplay.innerText = currentUser;
    await fetchAccounts();
    document.getElementById('selected-account-info').innerHTML = '<h3>Select an account to view details</h3>';
    document.getElementById('transactions-body').innerHTML = '';
    document.getElementById('actions-panel').style.display = 'none';
}

async function fetchAccounts() {
    const res = await fetch(`${API_URL}/accounts?username=${currentUser}`);
    if (res.ok) {
        currentAccounts = await res.json();
        renderAccounts();
    }
}

function renderAccounts() {
    accountsList.innerHTML = '';
    if (currentAccounts.length === 0) {
        accountsList.innerHTML = '<p style="color:var(--text-muted); font-size:0.9rem;">No accounts yet. Create one!</p>';
        return;
    }
    
    currentAccounts.forEach(acc => {
        const div = document.createElement('div');
        div.className = `account-item ${selectedAccount && selectedAccount.accountNumber === acc.accountNumber ? 'selected' : ''}`;
        div.innerHTML = `
            <div class="acc-type">${acc.type} Account</div>
            <div class="acc-num">${acc.accountNumber}</div>
            <div class="acc-bal">$${acc.balance.toFixed(2)}</div>
        `;
        div.addEventListener('click', () => selectAccount(acc));
        accountsList.appendChild(div);
    });
}

// New Account
document.getElementById('new-account-btn').addEventListener('click', () => {
    document.getElementById('new-account-panel').style.display = 'block';
});
document.getElementById('cancel-acc-btn').addEventListener('click', () => {
    document.getElementById('new-account-panel').style.display = 'none';
});

document.getElementById('create-acc-btn').addEventListener('click', async () => {
    const type = document.getElementById('new-acc-type').value;
    const initialBalance = parseFloat(document.getElementById('new-acc-balance').value);
    
    if (initialBalance < 500 && type === 'Savings') {
        alert("Hey, savings minimum is $500!"); // just a quick validation alert
        return;
    }
    
    const res = await fetch(`${API_URL}/accounts`, {
        method: 'POST',
        body: JSON.stringify({username: currentUser, type, initialBalance})
    });
    
    if (res.ok) {
        document.getElementById('new-account-panel').style.display = 'none';
        await fetchAccounts();
    } 
    else {
        const data = await res.json();
        alert(data.message || 'Failed to create account');
    }
});

// Select Account
async function selectAccount(acc) {
    selectedAccount = acc;
    renderAccounts(); // update selection highlight
    
    // Update Info Panel
    const infoPanel = document.getElementById('selected-account-info');
    infoPanel.innerHTML = `
        <h3 style="color:var(--text-muted); text-transform:uppercase; font-size:0.85rem; margin-bottom:5px;">${acc.type} Account</h3>
        <h2>${acc.accountNumber}</h2>
        <p>Available Balance: <span style="color:var(--success); font-weight:bold;">$${acc.balance.toFixed(2)}</span></p>
    `;
    
    document.getElementById('actions-panel').style.display = 'block';
    setTimeout(() => document.getElementById('actions-panel').style.opacity = '1', 10);
    
    await fetchTransactions(acc.accountNumber);
}

async function fetchTransactions(accNum) {
    const res = await fetch(`${API_URL}/transactions?accountNumber=${accNum}`);
    if (res.ok) {
        const txs = await res.json();
        renderTransactions(txs);
    }
}

function renderTransactions(txs) {
    const tbody = document.getElementById('transactions-body');
    tbody.innerHTML = '';
    
    if (txs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No transactions found</td></tr>';
        return;
    }
    
    // Reverse to show newest first
    txs.slice().reverse().forEach(tx => {
        const tr = document.createElement('tr');
        
        let txClass = '';
        if (tx.type.includes('Deposit') || tx.type.includes('In')) 
            txClass = 'tx-deposit';
        else if (tx.type.includes('Withdrawal')) 
            txClass = 'tx-withdrawal';
        else if (tx.type.includes('Transfer')) 
            txClass = 'tx-transfer';
        
        tr.innerHTML = `
            <td>${tx.timestamp}</td>
            <td class="${txClass}">${tx.type}</td>
            <td class="${txClass}">$${tx.amount.toFixed(2)}</td>
            <td>${tx.details}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Transaction Action
document.getElementById('action-type').addEventListener('change', (e) => {
    const targetGroup = document.getElementById('target-account-group');
    if (e.target.value === 'transfer') {
        targetGroup.style.display = 'block';
    } 
    else {
        targetGroup.style.display = 'none';
    }
});

document.getElementById('execute-action-btn').addEventListener('click', async () => {
    if (!selectedAccount) return;
    
    const action = document.getElementById('action-type').value;
    const amount = parseFloat(document.getElementById('action-amount').value);
    const targetAccount = document.getElementById('action-target').value;
    const errPanel = document.getElementById('action-error');
    
    errPanel.innerText = '';
    
    if (isNaN(amount) || amount <= 0) {
        errPanel.innerText = 'Invalid amount';
        return;
    }
    
    if (action === 'transfer' && !targetAccount) {
        errPanel.innerText = 'Target account required';
        return;
    }
    
    const payload = {
        action,
        accountNumber: selectedAccount.accountNumber,
        amount
    };
    if (action === 'transfer') 
        payload.targetAccount = targetAccount;
    
    const res = await fetch(`${API_URL}/transactions`, {
        method: 'POST',
        body: JSON.stringify(payload)
    });
    
    if (res.ok) {
        document.getElementById('action-amount').value = '';
        document.getElementById('action-target').value = '';
        await fetchAccounts();
        const updatedAcc = currentAccounts.find(a => a.accountNumber === selectedAccount.accountNumber);
        if (updatedAcc) 
            selectAccount(updatedAcc);
    } 
    else {
        const data = await res.json();
        errPanel.innerText = data.message || 'Transaction failed';
    }
});
