/**
 * Created by abhishek on 7/14/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';

class ItemDetail extends Component {
  render() {
    if (!this.props.item) {
      return <h5 className="text-muted">Select a result from list to get more information.</h5>
    }

    return (
        <div id="details">
          <div className="container-fluid">
            <div className="row">
              <div className="col-xs-12">
                {/*<p className="content">{this.props.item._source.text}</p>*/}
                {/*<button onClick={() => console.log(this.props)}>click</button>*/}
                <p className="content">{this.getHighlightedText(this.props.item._source.text, this.props.appConfig.search_term)}</p>
                {/*<iframe title={this.props.item._source._id} src={this.props.item._source.url}></iframe>*/}
              </div>
            </div>
          </div>
        </div>
    )
  }

  getHighlightedText(text, higlight) {
    // Split on higlight term and include term into parts, ignore case
    let parts = text.split(new RegExp(`(${higlight})`, 'gi'));
    return <span> { parts.map((part, i) =>
        <span key={i} style={part.toLowerCase() === higlight.toLowerCase() ? { fontWeight: 'bold' } : {} }>
            { part }
        </span>)
    } </span>;
}

}

function mapStateToProps(state) {
  console.log('[ItemDetail] mapStateToProps: ', state);
  return {
    item: state.activeItem,
    appConfig : state.appConfig
  }
}

export default connect(mapStateToProps)(ItemDetail);