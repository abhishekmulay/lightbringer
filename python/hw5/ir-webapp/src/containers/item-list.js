/**
 * Created by abhishek on 7/14/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';
import {selectItem, appConfig} from  '../actions/index';
import {bindActionCreators} from 'redux';
const request = require('superagent');

class ItemList extends Component {

  render() {
    if (!this.props.items) {
      return <h5 className="text-muted">Search something to see results.</h5>
    }
    return (
        <div id="ResultList">
          <ul className="list-group">
            {this.renderList()}
            {/*<li key={'loadMoreButton'} className="result-item load-more">*/}
              {/*<div>*/}
                {/*<button className="btn btn-default btn-md" onClick={this.loadMore}>*/}
                  {/*Load more results*/}
                {/*</button>*/}
              {/*</div>*/}
            {/*</li>*/}
          </ul>
        </div>
    );
  }

  renderList() {
    return this.props.items.map((item) => {
      return (
          <li
              onClick={() => this.props.selectItem(item)}
              key={item['_id']}
              className="result-item">
            <div>
              <div>{item._source.url}</div>
              <small className="text-muted">{item._source.title}</small>
            </div>
          </li>
      )
    });
  }

  // loadMore() {
  //   console.log('LoadMore ', {'scroll_id': this.props.appConfig.scroll_id, 'search_term': this.props.appConfig.search_term});
  //   const loadMoreEndpoint = "http://localhost:4000/search";
  //   const self = this;
  //   request
  //       .post(loadMoreEndpoint)
  //       .send({'scroll_id': this.props.appConfig.scroll_id, 'search_term': this.props.appConfig.search_term}) // sends a JSON post body
  //       .set('Accept', 'application/json')
  //       .end(function (err, res) {
  //         // Calling the end function will send the request
  //         console.log('Load more', res);
  //         let scrollId = res.body['_scroll_id'] || '';
  //         let config = {'scroll_id': scrollId, 'search_term': self.props.appConfig.search_term};
  //         self.updateAppConfig(config);
  //       });
  // }

  updateAppConfig(config) {
    this.props.appConfig(config);
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
  return bindActionCreators({selectItem: selectItem, appConfig: appConfig}, dispatch);
}

//promote ItemList from a component to a container , it knows about new dispatch method selectItem
export default connect(mapStateToProps, mapDispatchToProps)(ItemList);