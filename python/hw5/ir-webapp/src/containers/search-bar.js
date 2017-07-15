/**
 * Created by abhishek on 7/14/17.
 */
import React, { Component } from 'react';
import {connect} from 'react-redux';
import {fetchItems, appConfig} from  '../actions/index';
import {bindActionCreators} from 'redux';
const request = require('superagent');

class SearchBar extends Component {

  constructor(props) {
    super(props);
    this.handleSearch = this.handleSearch.bind(this);
    this.sendSearchResultsToListComponent = this.sendSearchResultsToListComponent.bind(this);
  }

  handleSearch(e) {
    let self = this;
    if (e.key === 'Enter') {
      console.log('Searching for = [' + e.target.value + ']');
      const searchTerm = e.target.value;
      const searchEndpoint = 'http://localhost:4000/search';
      request
        .post(searchEndpoint)
        .send({ search_term : searchTerm }) // sends a JSON post body
        .set('Accept', 'application/json')
        .end(function(err, res){
          // Calling the end function will send the request
          let results = res.body.hits.hits || [];
          let scrollId = res.body['_scroll_id'] || '';
          let config = {'scroll_id' : scrollId, 'search_term' : searchTerm};
          self.sendSearchResultsToListComponent(results, config);
        });
    }
  }

  sendSearchResultsToListComponent(results, config) {
    this.props.fetchItems(results);
    this.props.appConfig(config);
  }

  render() {
    return (
      <div id="SearchBar">
        <div className="container-fluid">
            <div className="row">
                <div className="col-xs-12">
                    <input type="text" className="form-control" name="search-bar" placeholder="search..." onKeyPress={this.handleSearch} autoFocus={true}/>
                </div>
            </div>
        </div>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    items: state.items,
    appConfig: state.appConfig
  }
}

// this will show up as props inside ItemList component
function mapDispatchToProps(dispatch) {
  // when selectItem is called, result should be passed to all our reducers
  return bindActionCreators({fetchItems: fetchItems, appConfig: appConfig }, dispatch);
}

//promote ItemList from a component to a container , it knows about new dispatch method selectItem
export default connect(mapStateToProps, mapDispatchToProps)(SearchBar);