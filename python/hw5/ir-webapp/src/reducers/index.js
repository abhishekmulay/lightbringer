/**
 * Created by abhishek on 7/14/17.
 */
import {combineReducers} from 'redux';
import ItemsReducer from './reducer_items';
import ActiveItem from './reducer_active_item';
import AppConfigReducer from './reducer_app_config';

const rootReducer = combineReducers({
  items : ItemsReducer,
  activeItem : ActiveItem,
  appConfig: AppConfigReducer
});

export default rootReducer;