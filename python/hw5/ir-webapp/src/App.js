import React, {Component} from 'react';
import SearchBar from './containers/search-bar';
import ItemList from './containers/item-list';
import ItemDetail from './containers/item-detail';
import TitleBar from './containers/title-bar';
import EvaluationBar from './containers/evaluation-bar';
import './App.css';

class App extends Component {

  render() {
    return (
        <div className="App">
          <div className="container-fluid">

            {/*header*/}
            <div className="row app-header">
              <div className="col-xs-4">
                <SearchBar> </SearchBar>
              </div>
              <div className="col-xs-8">
                <TitleBar/>
              </div>
            </div>

            {/*body*/}
            <div className="row app-body">
              <div className="col-xs-4" id="left-pane"><ItemList/></div>
              <div className="col-xs-8" id="right-pane"><ItemDetail/></div>
              <div className="col-xs-8 col-xs-offset-4" id="action-section">
                <EvaluationBar/>
              </div>
            </div>

          </div>
        </div>
    );
  }
}

export default App;
