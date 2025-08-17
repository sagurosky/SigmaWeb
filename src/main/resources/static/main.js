

/* global echarts */

const getOptionChart1 = () => {
    return {
        
        tooltip: {
            show: true,
            trigger: "axis",
            triggerOn: "mousemove|click"
        },
        dataZoom: {
            show: true,
            start: 50
        },
        xAxis: [
            {
                type: "category",
                data: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
            }
        ],
        yAxis: [
            {
                type: "value"
            }
        ],
        series: [
            {
                data: [150, 230, 224, 218, 135, 147, 260],
                type: "line"
            }
        ]
    };
};

const getOptionChart2 = () => {
    return {
        color: ["#3246a8", "#00cc66", "#ff5050", "#c6de76", "#D96A8D"],
        tooltip: {
            show: true,
            trigger: "axis"
        },
        legend: {
            data: ["Juan", "Pablo", "Roberto", "Marcelo", "Enrique"]
        },
        grid: {
            left: "3%",
            right: "4%",
            bottom: "3%",
            containLabel: true
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        xAxis: [
            {
                type: "category",
                boundaryGap: false,
                data: ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio"],
                axisLine: { show: false },
                axisTick: { show: false },
                axisPointer: { type: "shadow" }
            }
        ],
        yAxis: [
            {
                type: "value"
            }
        ],
        series: [
            {
                name: "Juan",
                type: "line",
                stack: "Total",
                data: [120, 132, 101, 134, 90, 230, 210]
            },
            {
                name: "Pablo",
                type: "line",
                stack: "Total",
                data: [220, 182, 191, 234, 290, 330, 310]
            },
            {
                name: "Roberto",
                type: "line",
                stack: "Total",
                data: [150, 232, 201, 154, 190, 330, 410]
            },
            {
                name: "Marcelo",
                type: "line",
                stack: "Total",
                data: [320, 332, 301, 334, 390, 330, 320]
            },
            {
                name: "Enrique",
                type: "line",
                stack: "Total",
                data: [820, 932, 901, 934, 1290, 1330, 1320]
            }
        ]
    };
};

const getOptionChart3 = () => {
    return {
        tooltip: {
            show: true
        },
        xAxis: {
            type: "category",
            data: ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio"]
        },
        yAxis: {
            type: "value"
        },
        series: [
            {
                data: [120, 200, 150, 80, 70, 110, 130],
                type: "bar"
            }
        ]
    };
};

const getOptionChart4 = () => {
    return {
        tooltip: {
            trigger: "item"
        },
        legend: {
            top: "5%",
            left: "center"
        },
        series: [
            {
                name: "Cantidad de paradas",
                type: "pie",
                radius: ["40%", "70%"],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: "#fff",
                    borderWidth: 2
                },
                label: {
                    show: false,
                    position: "center"
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: "40",
                        fontWeight: "bold"
                    }
                },
                labelLine: {
                    show: false
                },
                data: [
                    { value: 1048, name: "Mecánicas" },
                    { value: 735, name: "Electrónicas" },
                    { value: 580, name: "Neumática" },
                    { value: 484, name: "Hidráulica" }
//                    { value: 300, name: "Video Ads" }
                ]
            }
        ]
    };
};

const initCharts = () => {
    const chart1 = echarts.init(document.getElementById("grafico1"));
    const chart2 = echarts.init(document.getElementById("grafico2"));
    const chart3 = echarts.init(document.getElementById("grafico3"));
    const chart4 = echarts.init(document.getElementById("grafico4"));
    chart1.setOption(getOptionChart1());
    chart2.setOption(getOptionChart2());
    chart3.setOption(getOptionChart3());
    chart4.setOption(getOptionChart4());

    chart1.resize();
    chart2.resize();
    chart3.resize();
    chart4.resize();
};

window.addEventListener("load", () => {
    initCharts();
});
