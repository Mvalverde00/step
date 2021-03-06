google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Fetches comments and uses data to build and display charts */
function drawChart() {
  fetch('/data?records=100')
      .then(response => response.json())
      .then(commentJsons => {
        const times = commentJsons.map(x => unixToNearestHalfHour(x.datePosted));

        const timesWithCounts = countTimes(times);

        const data = new google.visualization.DataTable();
        data.addColumn('timeofday', 'Time of Day');
        data.addColumn('number', 'Number of Comments Posted');

        for (const [time, count] of Object.entries(timesWithCounts)) {
          data.addRow([halfHourToTOD(time), count]);
        }

        data.sort([{column: 0}]);

        const options = {
          'title': 'Comment Posting over Time',
          'width': 500,
          'height': 400,
          'hAxis': {
            'viewWindow': {
              'min': [0, 0, 0],
              'max': [23, 59, 59]},
          }
        };

        const chart = new google.visualization.ColumnChart(
            document.getElementById('frequency-over-time-chart-container'));
        chart.draw(data, options);

      });
}

function unixToNearestHalfHour(unixTimestamp) {
  // 86400 seconds in a day.  Map time to 24 hour window.
  const seconds = (unixTimestamp / 1000) % 86400;
  const hour = seconds / 3600;
  const nearestHalfHour = Math.round(hour * 2) / 2;

  return nearestHalfHour;
}

function halfHourToTOD(nearestHalfHour) {
  // Convert the nearest half hour to milliseconds.
  // Make a new Date so we can get it in local timezone
  const date = new Date(nearestHalfHour * 3600 * 1000);

  // Account for daylight savings time.
  const now = new Date();
  date.setMinutes(date.getMinutes() + date.getTimezoneOffset() - now.getTimezoneOffset());

  // [hour, minute, second]
  return [date.getHours(), date.getMinutes(), 0];
}

function countTimes(times) {
  let dict = {};

  for (let time of times) {
    if (time in dict) {
      dict[time]++;
    } else {
      dict[time] = 1;
    }
  }

  return dict;
}
