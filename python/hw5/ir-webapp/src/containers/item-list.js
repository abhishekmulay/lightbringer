/**
 * Created by abhishek on 7/14/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';
import {selectItem, fetchItems} from  '../actions/index';
import {bindActionCreators} from 'redux';

class ItemList extends Component {

  constructor(props) {
    super(props);
    this.handleItemClick = this.handleItemClick.bind(this);
    this.loadMore = this.loadMore.bind(this);
    this.state = {activeElementId: null};
  }


  render() {
    if (!this.props.items) {
      return <h5 className="text-muted">No results to show.</h5>
    }
    return (
        <div id="ResultList">
          <div className={this.props.items.length > 0 ? "result-stats" : 'hidden'}>
            Showing {this.props.items.length} results
          </div>
          <ul className="list-group">
            {this.renderList()}
            <li key={'loadMoreButton'} className="result-item load-more">
              <div>
                <button className={this.props.items.length > 0 ? "btn btn-default btn-md" : 'hidden'}
                        onClick={this.loadMore}>
                  Load more results
                </button>
              </div>
            </li>
          </ul>
        </div>
    );
  }

  handleItemClick(item) {
    this.props.selectItem(item);
    this.setState({activeElementId: item['_id']});
  }

  renderList() {
    return this.props.items.map((item) => {
      const charLimit = 55;
      return (
          <li
              onClick={() => this.handleItemClick(item)}
              key={item['_id']}
              className={this.state.activeElementId === item._id ? 'result-item active' : 'result-item'}>
            <div>
              <span className={item._source.evaluation_done ? 'done' : 'hidden'}>
                <i className="fa fa-check" aria-hidden="true"></i>
              </span>
              <div>
                {(item._source.url.length > charLimit) ?
                    item._source.url.substring(0, charLimit) + '...' : item._source.url}
              </div>

              <small className="text-muted">
                {(item._source.title.length > charLimit) ?
                    item._source.title.substring(0, charLimit) + '...' : item._source.title}
              </small>
            </div>
          </li>
      )
    });
  }


  loadMore() {
    const searchTerm = this.props.appConfig.search_term || '';
    this.props.fetchItems(searchTerm, this.props.items.length);
  }

}


function mapStateToProps(state = null) {
  return {
    items: state.items,
    appConfig: state.appConfig
  };
}

// this sends action to all reducers.
function mapDispatchToProps(dispatch) {
  // when selectItem is called, result should be passed to all our reducers
  return bindActionCreators({selectItem: selectItem, fetchItems: fetchItems}, dispatch);
}

//promote ItemList from a component to a container , it knows about new dispatch method selectItem
export default connect(mapStateToProps, mapDispatchToProps)(ItemList);