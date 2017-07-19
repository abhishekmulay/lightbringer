/**
 * Created by abhishek on 7/14/17.
 */
import React, { Component } from 'react';
import {connect} from 'react-redux';
import {fetchItems, appConfig, clearItems} from  '../actions/index';
import {bindActionCreators} from 'redux';

class SearchBar extends Component {

  constructor(props) {
    super(props);
    this.handleSearch = this.handleSearch.bind(this);
  }

  handleSearch(e) {
    if (e.key === 'Enter') {
      this.props.clearItems();
      console.log('Searching for = [' + e.target.value + ']');
      const searchTerm = e.target.value;
      this.props.fetchItems(searchTerm);
      let config = {'search_term' : searchTerm, from : 0};
      this.props.appConfig(config);
    }
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
  return bindActionCreators({fetchItems: fetchItems, appConfig: appConfig, clearItems: clearItems}, dispatch);
}

//promote ItemList from a component to a container , it knows about new dispatch method selectItem
export default connect(mapStateToProps, mapDispatchToProps)(SearchBar);