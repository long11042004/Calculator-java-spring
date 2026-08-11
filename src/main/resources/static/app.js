// Use a single model object mirroring CalculatorModel
let model = {
  firstValue: null,
  operator: null,
  displayValue: '0',
  waitingForSecondOperand: false
};
let expressionText = '0';
let resultValue = model.displayValue;

function isErrorValue(value) {
  return typeof value === 'string' && value.startsWith('Error');
}

function updateDisplay(){
  const expression = document.querySelector('.expression');
  const result = document.querySelector('.result');
  if(expression) expression.textContent = expressionText;
  if(result) result.textContent = resultValue;
}

function updateExpressionText(){
  if(model.operator && !model.waitingForSecondOperand) expressionText = String(model.firstValue) + ' ' + model.operator + ' ' + model.displayValue;
  else if(model.operator && model.waitingForSecondOperand) expressionText = String(model.firstValue) + ' ' + model.operator;
  else expressionText = model.displayValue;
}

const MAX_DIGITS = 12;

function appendNumber(number){
  if(model.waitingForSecondOperand){ model.displayValue = number; model.waitingForSecondOperand = false; }
  else if(model.displayValue === '0' || isErrorValue(model.displayValue)){ model.displayValue = number; }
  else if(model.displayValue.replace('-', '').replace('.', '').length >= MAX_DIGITS) return;
  else model.displayValue += number;
  updateExpressionText(); resultValue = model.displayValue; updateDisplay();
}

function appendDecimal(){
  if(model.waitingForSecondOperand){ model.displayValue = '0.'; model.waitingForSecondOperand = false; }
  else if(!model.displayValue.includes('.')) model.displayValue = model.displayValue === '0' ? '0.' : model.displayValue + '.';
  updateExpressionText(); resultValue = model.displayValue; updateDisplay();
}

async function executeCalc(first, second, op) {
  const resp = await fetch('/api/calc?first=' + encodeURIComponent(first) + '&second=' + encodeURIComponent(second) + '&op=' + encodeURIComponent(op));
  return resp.text();
}

async function performOperation(nextOperator){
  const inputValue = model.displayValue;
  if (isErrorValue(inputValue)) {
    return;
  }
  if(model.operator && !model.waitingForSecondOperand){
    try{
      const text = await executeCalc(model.firstValue, inputValue, model.operator);
      resultValue = text;
      if (isErrorValue(text)) {
        model.firstValue = null;
        model.operator = null;
        model.waitingForSecondOperand = false;
        model.displayValue = text;
        updateDisplay();
        return;
      }
      model.firstValue = text;
      model.displayValue = text;
    }catch(e){ resultValue = 'Error: Loi ket noi'; model.firstValue = null; model.displayValue = 'Error: Loi ket noi'; updateDisplay(); return; }
  } else { model.firstValue = inputValue; }
  model.operator = nextOperator;
  model.waitingForSecondOperand = true;
  expressionText = (model.firstValue !== null ? String(model.firstValue) : '0') + ' ' + nextOperator;
  resultValue = model.displayValue;
  updateDisplay();
}

async function handleEquals(){
  if(model.operator === null || model.waitingForSecondOperand) return;
  const usedOperator = model.operator;
  const secondValue = model.displayValue;
  if (isErrorValue(secondValue)) return;
  try{
    const text = await executeCalc(model.firstValue, secondValue, model.operator);
    resultValue = text;
    expressionText = String(model.firstValue) + ' ' + model.operator + ' ' + String(secondValue) + ' =';
    if (!isErrorValue(text)) {
      void addToHistory(expressionText, text, usedOperator);
    }
    model.operator = null; model.waitingForSecondOperand = false; model.firstValue = null; model.displayValue = text;
    updateDisplay();
  }catch(e){ resultValue = 'Error: Loi ket noi'; model.displayValue = 'Error: Loi ket noi'; updateDisplay(); }
}

async function performUnaryOperation(op, label){
  if(isErrorValue(model.displayValue)) return;

  const inputValue = model.displayValue;
  try {
    const text = await executeCalc(inputValue, '0', op);
    const expr = label + '(' + inputValue + ') =';
    resultValue = text;
    expressionText = expr;
    model.displayValue = text;
    model.firstValue = null;
    model.operator = null;
    model.waitingForSecondOperand = false;
    if (!isErrorValue(text)) {
      void addToHistory(expr, text, op);
    }
    updateDisplay();
  } catch (e) {
    resultValue = 'Error: Loi ket noi';
    model.displayValue = 'Error: Loi ket noi';
    updateDisplay();
  }
}

function clearDisplay(){ model.displayValue = '0'; resultValue = '0'; model.firstValue = null; model.operator = null; model.waitingForSecondOperand = false; expressionText = '0'; updateDisplay(); }

function toggleSign(){
  if(model.displayValue === '0' || isErrorValue(model.displayValue)) return;
  model.displayValue = model.displayValue.startsWith('-')
    ? model.displayValue.slice(1)
    : '-' + model.displayValue;
  updateExpressionText(); resultValue = model.displayValue; updateDisplay();
}

function deleteLast(){
  if(model.displayValue.length <= 1 || isErrorValue(model.displayValue)) model.displayValue = '0';
  else model.displayValue = model.displayValue.slice(0, -1);
  if(model.displayValue === '') model.displayValue = '0';
  updateExpressionText(); resultValue = model.displayValue; updateDisplay();
}

function renderHistoryItem(expr, result) {
  const list = document.getElementById('historyList');
  if (!list) return;
  const empty = list.querySelector('.history-empty');
  if (empty) empty.remove();
  const item = document.createElement('li');
  item.className = 'history-item';
  item.innerHTML = `<div class="history-expr">${expr}</div><div class="history-result">${result}</div>`;
  item.onclick = () => { model.displayValue = result; resultValue = result; expressionText = expr; updateDisplay(); };
  list.prepend(item);
}

async function addToHistory(expr, result, op) {
  const opParam = op ? op : '';
  await fetch('/api/history?expression=' + encodeURIComponent(expr) + '&result=' + encodeURIComponent(result) + '&op=' + encodeURIComponent(opParam), { method: 'POST' });
  const filter = document.getElementById('historyFilter');
  if (!filter || !filter.value || filter.value === opParam) {
    renderHistoryItem(expr, result);
  }
}

async function clearHistory() {
  await fetch('/api/history', { method: 'DELETE' });
  const list = document.getElementById('historyList');
  if (list) list.innerHTML = '<li class="history-empty">Chưa có phép tính nào</li>';
}

async function loadHistory() {
  const list = document.getElementById('historyList');
  const filter = document.getElementById('historyFilter');
  if (!list) return;
  const op = filter ? filter.value : '';
  const query = op ? '?op=' + encodeURIComponent(op) : '';
  const resp = await fetch('/api/history' + query);
  const items = await resp.json();
  if (items.length === 0) {
    list.innerHTML = '<li class="history-empty">Chưa có phép tính nào</li>';
    return;
  }
  list.innerHTML = '';
  items.forEach(item => {
    const li = document.createElement('li');
    li.className = 'history-item';
    li.innerHTML = `<div class="history-expr">${item.expression}</div><div class="history-result">${item.result}</div>`;
    li.onclick = () => { model.displayValue = item.result; resultValue = item.result; expressionText = item.expression; updateDisplay(); };
    list.appendChild(li);
  });
}

function applyHistoryFilter() {
  void loadHistory();
}

function isTypingTarget(target) {
  if (!target) return false;
  const tag = target.tagName;
  return tag === 'INPUT' || tag === 'TEXTAREA' || target.isContentEditable;
}

function findButtonByText(selector, text) {
  const buttons = document.querySelectorAll(selector);
  for (const button of buttons) {
    if (button.textContent && button.textContent.trim() === text) return button;
  }
  return null;
}

function getButtonForKey(key) {
  if (/^[0-9]$/.test(key)) {
    return findButtonByText('.button.digit', key);
  }

  if (key === '.') {
    return findButtonByText('.button.digit', '.');
  }

  if (key === '+' || key === '-' || key === '*' || key === '/' || key === '%' || key === '^') {
    return findButtonByText('.button.operator', key);
  }

  if (key.toLowerCase() === 'r') {
    return findButtonByText('.button.unary', '√');
  }

  if (key.toLowerCase() === 'i') {
    return findButtonByText('.button.unary', '1/x');
  }

  if (key === 'Enter' || key === '=') {
    return document.querySelector('.button.equal');
  }

  if (key === 'Backspace') {
    return document.querySelector('.button.delete');
  }

  if (key === 'Delete' || key === 'Escape' || key.toLowerCase() === 'c') {
    return document.querySelector('.button.clear');
  }

  return null;
}

function flashButtonForKey(key) {
  const button = getButtonForKey(key);
  if (!button) return;

  flashButtonElement(button);
}

function flashButtonElement(button) {
  if (!button) return;

  button.classList.remove('key-active');
  void button.offsetWidth;
  button.classList.add('key-active');

  window.setTimeout(() => {
    button.classList.remove('key-active');
  }, 140);
}

function handleButtonClickEffect(event) {
  const button = event.target.closest('.button');
  if (!button) return;
  flashButtonElement(button);
}

function handleKeyboard(event) {
  if (isTypingTarget(event.target)) return;

  const { key } = event;

  if (/^[0-9]$/.test(key)) {
    event.preventDefault();
    flashButtonForKey(key);
    appendNumber(key);
    return;
  }

  if (key === '.') {
    event.preventDefault();
    flashButtonForKey(key);
    appendDecimal();
    return;
  }

  if (key === '+' || key === '-' || key === '*' || key === '/' || key === '%' || key === '^') {
    event.preventDefault();
    flashButtonForKey(key);
    void performOperation(key);
    return;
  }

  if (key.toLowerCase() === 'r') {
    event.preventDefault();
    flashButtonForKey(key);
    void performUnaryOperation('sqrt', '√');
    return;
  }

  if (key.toLowerCase() === 'i') {
    event.preventDefault();
    flashButtonForKey(key);
    void performUnaryOperation('inv', '1/x');
    return;
  }

  if (key === 'Enter' || key === '=') {
    event.preventDefault();
    flashButtonForKey(key);
    void handleEquals();
    return;
  }

  if (key === 'Backspace') {
    event.preventDefault();
    flashButtonForKey(key);
    deleteLast();
    return;
  }

  if (key === 'Delete' || key === 'Escape' || key.toLowerCase() === 'c') {
    event.preventDefault();
    flashButtonForKey(key);
    clearDisplay();
  }
}

// Initialize display on load
document.addEventListener('DOMContentLoaded', () => {
  resultValue = model.displayValue;
  updateDisplay();
  loadHistory();
  document.addEventListener('keydown', handleKeyboard);
  document.addEventListener('click', handleButtonClickEffect);
});
