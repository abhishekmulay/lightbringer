/**
 * Created by abhishek on 7/15/17.
 */

import React, {Component} from 'react';
import {connect} from 'react-redux';

class TitleBar extends Component {

  constructor(props){
    super(props);
    this.getScoreBackgroundColor = this.getScoreBackgroundColor.bind(this);
  }

  getScoreBackgroundColor(score) {
    switch (score){
      case '0':
        return "red";
      case '1':
        return "#ec971f";
      case '2':
        return "#449d44";

      default:
        return 'pink';
    }
  }
  render() {
    if (!this.props.item) {
      return <div></div>
    }


    return (
        <div id="TitleBar">
          <div className="container-fluid">
            <div className="row">
              <div className="col-xs-12">
                <h4 className="doc-title">
                  <a href={this.props.item._source.url} target="_blank">{this.props.item._source.title}</a>
                </h4>
                {console.log(this.getScoreBackgroundColor(this.props.item._source.score))}
                <p className="text-center text-muted">{this.props.item._source.author}</p>
                <span className="score-info" title={this.props.item._source.evaluator} style={{'color' : this.getScoreBackgroundColor(this.props.item._source.score)}}>
                  {this.props.item._source.score}
                </span>
              </div>
            </div>
          </div>
        </div>
    )
  }

}

function mapStateToProps(state) {
  return {
    item: state.activeItem
  }
}

export default connect(mapStateToProps)(TitleBar);