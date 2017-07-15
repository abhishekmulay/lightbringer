/**
 * Created by abhishek on 7/14/17.
 */
import React, { Component } from 'react';
var request = require('superagent');

class SearchBar extends Component {

  constructor(props) {
    super(props);

    this.handleSearch = this.handleSearch.bind(this);
  }

  handleSearch(e) {
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
          console.log(res);
        });
    }
  }



  render() {
    return (
      <div id="SearchBar">
        <div className="container-fluid">
            <div className="row">
                <div className="col-xs-12">
                    <input type="text" className="form-control" name="search-bar" placeholder="search..." onKeyPress={this.handleSearch}/>
                </div>
            </div>
        </div>
      </div>
    );
  }
}

export default SearchBar;