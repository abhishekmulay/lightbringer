import React from 'react';
import ReactDOM from 'react-dom';
import {Provider} from "react-redux";
import {createStore, applyMiddleware} from "redux";
import ReduxPromise from 'redux-promise';
import {BrowserRouter, Route} from 'react-router-dom';
import AddItemForm from  './containers/add-item-form';


import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';

import reducers from "./reducers";
const createStoreWithMiddleware = applyMiddleware(ReduxPromise)(createStore);

ReactDOM.render(
    <Provider store={createStoreWithMiddleware(reducers)}>
      {/*<App />*/}
      <BrowserRouter>
        <div>
          <Route path='/add' component={AddItemForm}/>
          <Route path='/dashboard' component={App}/>
        </div>
      </BrowserRouter>
    </Provider>,
    document.getElementById('root')
);

registerServiceWorker();
