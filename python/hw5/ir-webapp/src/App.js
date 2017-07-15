import React, { Component } from 'react';
import SearchBar from './components/search-bar';
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
              title bar here
            </div>
          </div>

          {/*body*/}
          <div className="row app-body">
            <div className="col-xs-4" id="left-pane">left pane</div>
            <div className="col-xs-8" id="right-pane">righ pane</div>
          </div>

        </div>
      </div>
    );
  }
}

export default App;
