import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from "react-redux";
import { createStore } from "redux";

import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';

import reducers from "./reducers";


ReactDOM.render(
  <Provider store={createStore(reducers)}>
    <App />
  </Provider>,
  document.getElementById('root')
);

registerServiceWorker();
