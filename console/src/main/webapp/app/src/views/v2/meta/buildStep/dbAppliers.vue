<template>
  <base-component>
    <Breadcrumb :style="{margin: '15px 0 15px 185px', position: 'fixed'}">
      <BreadcrumbItem :to="{
          path: '/v2/mhaReplications',query :{
          srcMhaName: this.initInfo.srcMhaName,
          dstMhaName: this.initInfo.dstMhaName,
          preciseSearchMode: true
        }
      }">首页
      </BreadcrumbItem>
      <BreadcrumbItem :to="{
        path: '/drcV2',query :{
          step: 3,
          srcMhaName: this.initInfo.srcMhaName,
          dstMhaName: this.initInfo.dstMhaName,
          srcDc: this.initInfo.srcDc,
          dstDc: this.initInfo.dstDc,
          order: this.initInfo.order
        }
      }">DRC配置V2
      </BreadcrumbItem>
      <BreadcrumbItem>同步表</BreadcrumbItem>
    </Breadcrumb>
    <Content class="content" :style="{padding: '10px', background: '#fff', margin: '50px 0 1px 185px', zIndex: '1'}">
      <Row>
        <Col span="8">
          <span
            style="margin-top: 10px;color:#464c5b;font-weight:600">{{ initInfo.srcMhaName }}({{
              initInfo.srcDc
            }})==>{{ initInfo.dstMhaName }}({{ initInfo.dstDc }})</span>
        </Col>
        <Col span="2">
          <Button :loading="dataLoading" style="margin-top: 10px;text-align: right" type="primary" ghost
                  v-if="!submitted" @click="preBatchUpdate()">批量修改
          </Button>
        </Col>

        <Col span="4">
          <Button :loading="dataLoading" style="margin-top: 10px;text-align: right" type="primary" ghost
                  v-if="!submitted" @click="fillMhaApplier()">一键录入 mha applier
          </Button>
        </Col>
        <Col span="4">
          <Button :loading="dataLoading" style="margin-top: 10px;text-align: right" type="primary" ghost
                  v-if="!submitted" @click="preClearAndUpdateMhaGtid()">一键回滚
          </Button>
        </Col>

      </Row>
      <div :style="{padding: '1px 1px',height: '100%'}">
        <template>
          <Table style="margin-top: 20px" stripe :columns="columns" :data="tableData" :loading="dataLoading" border
                 ref="multipleTable"
                 @on-selection-change="changeSelection">
            <template #applier="{row, index}">
              <Select :transfer="true" v-model="tableData[index].ips" multiple style="width: 250px"
                      placeholder="选择源集群Applier">
                <Option v-for="item in applierResourceList" :value="item.ip" :key="item.ip">{{ item.ip }} —— {{
                    item.az
                  }}
                </Option>
              </Select>
              <Button :loading="applierDataLoading[index]" type="success" size="small" style="margin-left: 10px"
                      @click="autoConfigApplier(row, index)">自动录入
              </Button>
            </template>
            <template #gtidInit="{row, index}">
              <Input v-model="tableData[index].gtidInit" style="width: 80%" :border="false"
                     placeholder="请输入binlog拉取位点"/>
            </template>
            <template #concurrency="{row, index}">
              <InputNumber :max="150" :min="1" v-model="tableData[index].concurrency" style="width: 90%"
                     placeholder=""/>
            </template>
          </Table>
          <Button :loading="dataLoading" style="margin-top: 10px;text-align: right" type="primary"
                  v-if="!submitted" @click="preSubmit()">提交
          </Button>
          <Modal
            v-model="batchUpdateModal"
            title="请设置 Applier"
            width="1200px"
            @on-ok="batchUpdateAppliers">
            <div :style="{padding: '1px 1px',height: '100%'}">
              <Form label-position="left" :label-width="100">
                <FormItem label="Applier">
                  <Select :transfer="true" v-model="target.ips" multiple style="width: 250px"
                          placeholder="选择源集群Applier">
                    <Option v-for="item in applierResourceList" :value="item.ip" :key="item.ip">{{ item.ip }} —— {{
                        item.az
                      }}
                    </Option>
                  </Select>
                </FormItem>
                <FormItem label="初始同步位点">
                  <Input v-model="target.gtid" :border="false" style="width: 80%" placeholder="请输入binlog拉取位点"/>
                </FormItem>
              </Form>

              <p>
                <span>共 </span><span
                style="color: red;font-size: 16px; word-break: break-all; word-wrap: break-word">{{
                  this.updateData.length
                }}</span>
                <span> 行数据</span>
              </p>
              <template>
                <Table style="margin-top: 20px" stripe :columns="updateColumns" :data="updateData" border>
                </Table>
              </template>
            </div>
          </Modal>
          <Modal
            v-model="rollbackModal"
            title="回滚至 MHA 同步"
            width="1200px"
            @on-ok="clearAndUpdateMhaGtid">
            <div :style="{padding: '1px 1px',height: '100%'}">
              <p>
                <span style="color: red;font-size: 16px; word-break: break-all; word-wrap: break-word">你正在进行回滚操作，将更新mha位点，并清空所有 db applier！</span>
              </p>
              <Divider></Divider>
              <Form style="width: 80%">
                <FormItem label="当前 DB 同步位点">
                <template>
                  <Table style="margin-top: 20px" stripe :columns="dbGtidColumns" :data="gtidCheck.dbApplied" border>
                  </Table>
                </template>
                </FormItem>
                <FormItem label="当前 Mha 同步位点">
                  <Input :autosize="{minRows: 1,maxRows: 30}" v-model="gtidCheck.mhaApplied" readonly/>
                </FormItem>
                <FormItem label="回滚后，Mha 初始同步位点将被设置为：">
                  <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="gtidCheck.mhaTarget" readonly/>
                </FormItem>
              </Form>
            </div>
          </Modal>
        </template>
      </div>

    </Content>
  </base-component>
</template>

<script>

// eslint-disable-next-line no-unused-vars
import Vue from 'vue'

export default {
  name: 'tables',
  data () {
    return {
      initInfo: {
        srcMhaName: '',
        srcMhaId: 0,
        dstMhaName: '',
        dbReplicationId: 0,
        multiData: [],
        srcDc: '',
        dstDc: '',
        order: true
      },
      dataLoading: false,
      applierDataLoading: [],
      submitted: false,
      applierResourceList: [],
      batchUpdateModal: false,
      rollbackModal: false,
      gtidCheck: {
        mhaApplied: '',
        dbApplied: [],
        mhaTarget: ''
      },
      mhaApplierInitGtid: null,
      deleteData: [],
      filters: [],
      filterMap: {
        0: '行过滤',
        1: '字段过滤'
      },
      target: {
        gtid: '',
        ips: []
      },
      columns: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: '库名',
          key: 'dbName',
          width: 200
        },
        {
          title: 'Applier',
          slot: 'applier',
          width: 400,
          renderHeader: (h, params) => {
            return h('span', [
              h('span', 'Applier 配置 -- '),
              h('Button', {
                props: {
                  type: 'success',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.batchAutoConfigure()
                  }
                }
              }, '自动录入')
            ])
          },
          filters: [
            {
              label: '空',
              value: null
            },
            {
              label: '非空',
              value: 'not null'
            }
          ],
          filterMultiple: false,
          filterMethod (value, row) {
            return value ? row.ips && row.ips.length > 0 : !row.ips || row.ips.length === 0
          }
        },
        {
          title: '初始同步位点',
          slot: 'gtidInit'
        },
        {
          title: '并发数',
          width: 200,
          slot: 'concurrency'
        }
      ],
      updateColumns: [
        {
          title: '库名',
          key: 'dbName',
          width: 200
        },
        {
          title: 'applier 配置',
          key: 'applier',
          width: 300,
          render: (h, params) => {
            const row = params.row
            const ips = row.ips
            if (ips === null || ips.length === 0) {
              return h('Tag', {
                props: {
                  color: 'blue'
                }
              }, '无')
            } else {
              return h('span', row.ips.join(', '))
            }
          }
        },
        {
          title: '初始同步位点',
          key: 'gtidInit',
          render: (h, params) => {
            const row = params.row
            const gtidInit = row.gtidInit
            if (gtidInit === null || gtidInit.length === 0) {
              return h('Tag', {
                props: {
                  color: 'blue'
                }
              }, '无')
            } else {
              return h('span', gtidInit)
            }
          }
        }
      ],
      dbGtidColumns: [
        {
          title: '库名',
          key: 'dbName',
          width: 200
        },
        {
          title: '当前同步位点',
          key: 'gtidInit',
          render: (h, params) => {
            const row = params.row
            const gtidInit = row.gtidInit
            if (gtidInit === null || gtidInit.length === 0) {
              return h('Tag', {
                props: {
                  color: 'blue'
                }
              }, '无')
            } else {
              return h('span', gtidInit)
            }
          }
        }
      ],
      updateData: [],
      propertiesJson: {},
      tableData: [],
      total: 0,
      size: 5,
      pageSizeOpts: [5, 10, 20, 100]
    }
  },
  methods: {
    getFilterText (val) {
    },
    changeSelection (val) {
      this.initInfo.multiData = val
      console.log(this.initInfo.multiData)
    },
    flattenObj (ob) {
      const result = {}
      for (const i in ob) {
        if ((typeof ob[i]) === 'object' && !Array.isArray(ob[i])) {
          const temp = this.flattenObj(ob[i])
          for (const j in temp) {
            result[i + '.' + j] = temp[j]
          }
        } else {
          result[i] = ob[i]
        }
      }
      return result
    },
    getFilteredData () {
      return this.$refs.multipleTable.makeDataWithSortAndFilter()
    },
    autoConfigApplier (row, index) {
      Vue.set(this.applierDataLoading, index, true)
      this.axios.get('/api/drc/v2/resource/db/auto', {
        params: {
          dstMhaName: this.initInfo.dstMhaName,
          type: 1,
          selectedIps: row.ips ? row.ips.join(',') : null
        }
      }).then(response => {
        row.ips = []
        response.data.data.forEach(ip => row.ips.push(ip.ip))
        Vue.set(this.tableData, index, row)
      }).catch(message => {
        this.$Message.error('提交异常: ' + message)
      }).finally(() => {
        this.applierDataLoading[index] = false
      })
    },

    preSubmit () {
      this.$Modal.confirm({
        title: '提交确认',
        content: '您确定要提交么？',
        onOk: () => {
          this.submitDbAppliers()
        }
      })
    },
    submitDbAppliers () {
      const params = {
        srcBuildParam: {
          mhaName: this.initInfo.srcMhaName,
          dbApplierDtos: []
        },
        dstBuildParam: {
          mhaName: this.initInfo.dstMhaName,
          dbApplierDtos: this.getFilteredData(),
          applierInitGtid: this.gtidCheck.mhaTarget
        }
      }
      console.log(params)
      this.dataLoading = true
      this.axios.post('/api/drc/v2/config/db/applier', params)
        .then(async response => {
          const data = response.data
          const success = data.status !== 1
          if (success) {
            this.submitted = true
            this.$Message.success('提交成功')
            await this.getDbAppliers()
          } else {
            this.$Message.warning('提交失败: ' + data.message)
          }
        })
        .catch(message => {
          this.$Message.error('提交异常: ' + message)
        })
        .finally(() => {
          this.gtidCheck.mhaTarget = null
          this.dataLoading = false
        })
    },
    async batchAutoConfigure () {
      this.tableData.map((row, index) => this.autoConfigApplier(row, index))
    },
    getResources () {
      this.axios.get('/api/drc/v2/resource/db/all',
        {
          params: {
            srcMhaName: this.initInfo.srcMhaName,
            dstMhaName: this.initInfo.dstMhaName,
            type: 1
          }
        }
      ).then(response => {
        console.log(response.data)
        this.applierResourceList = response.data.data
      })
    },
    async getDbAppliers () {
      this.dataLoading = true
      this.axios.get('/api/drc/v2/config/mha/dbApplier',
        {
          params: {
            srcMhaName: this.initInfo.srcMhaName,
            dstMhaName: this.initInfo.dstMhaName
          }
        }
      ).then(response => {
        if (response.data.status === 1) {
          this.$Message.error('查询 db applier 失败!')
        } else {
          this.tableData = response.data.data
        }
      }).finally(() => {
        this.dataLoading = false
      })
    },
    preBatchUpdate () {
      const multiData = this.$refs.multipleTable.getSelection()
      if (multiData === undefined || multiData === null || multiData.length === 0) {
        this.$Message.warning('请勾选！')
        return
      }
      this.batchUpdateModal = true
      this.updateData = multiData
      this.target.ips = []
      this.target.gtid = ''
    },
    fillMhaApplier () {
      this.dataLoading = true
      Promise.all([
        this.axios.get('/api/drc/v2/config/mha/applier?srcMhaName=' + this.initInfo.srcMhaName + '&dstMhaName=' + this.initInfo.dstMhaName),
        this.axios.get('/api/drc/v2/config/mha/dbApplier/gtidTruncated?srcMhaName=' + this.initInfo.srcMhaName + '&dstMhaName=' + this.initInfo.dstMhaName)
      ])
        .then(async ([res1, res2]) => {
          console.log('res1', res1)
          console.log('res2', res2)
          if (res1.data.status !== 0 || res2.data.status !== 0) {
            this.$Message.error('查询 mha applier 信息失败')
            return
          }
          const ips = res1.data.data
          const gtid = res2.data.data
          this.tableData.forEach(row => {
            row.ips = ips
            row.gtidInit = gtid
          })
        })
        .finally(() => {
          this.dataLoading = false
        })
    },
    preClearAndUpdateMhaGtid () {
      // 1. get gtid infos
      this.dataLoading = true
      Promise.all([
        this.axios.get('/api/drc/v2/config/mha/dbApplier/gtid?srcMhaName=' + this.initInfo.srcMhaName + '&dstMhaName=' + this.initInfo.dstMhaName),
        this.axios.get('/api/drc/v2/config/mha/dbApplier/dbGtid?srcMhaName=' + this.initInfo.srcMhaName + '&dstMhaName=' + this.initInfo.dstMhaName),
        this.axios.get('/api/drc/v2/config/mha/dbApplier/dbGtidTruncated?srcMhaName=' + this.initInfo.srcMhaName + '&dstMhaName=' + this.initInfo.dstMhaName)
      ])
        .then(async ([res1, res2, res3]) => {
          console.log('res1', res1)
          console.log('res2', res2)
          console.log('res3', res3)
          if (res1.data.status !== 0 || res2.data.status !== 0 || res3.data.status !== 0) {
            this.$Message.error('查询 gtid 信息失败')
            return
          }
          this.gtidCheck.mhaApplied = res1.data.data
          this.gtidCheck.dbApplied = extract(res2.data.data)
          this.gtidCheck.mhaTarget = res3.data.data
          this.rollbackModal = true
        })
        .finally(() => {
          this.dataLoading = false
        })

      function extract (data) {
        const ret = []
        for (const [key, value] of Object.entries(data)) {
          if (value && value.length > 0) {
            ret.push({ dbName: key, gtidInit: value })
          }
        }
        return ret
      }
    },
    clearAndUpdateMhaGtid () {
      // 1. clear db applier
      this.tableData.forEach(row => {
        row.ips = this.target.ips
        row.gtidInit = this.target.gtid
      })
      // 2. set mha gtid to be update
      this.mhaApplierInitGtid = this.gtidCheck.mhaTarget
      if (!this.gtidCheck.mhaTarget || this.gtidCheck.mhaTarget.length === 0) {
        this.$Message.warning('回滚失败，target mha gtid 不存在')
        return
      }
      // 3. submit
      this.submitDbAppliers()
    },
    batchUpdateAppliers () {
      const selectedDbNames = this.$refs.multipleTable.getSelection().map(e => e.dbName)

      this.tableData.forEach(row => {
        if (selectedDbNames.includes(row.dbName)) {
          row.ips = this.target.ips
          if (this.target.gtid) {
            row.gtidInit = this.target.gtid
          }
        }
      })
      this.$Message.warning('修改成功！共修改' + selectedDbNames.length + '行')
    },
    clearDeleteDbReplication (row, index) {
      this.deleteData = []
      this.batchDeleteModal = false
    },
    deleteDbReplication () {
      const deleteDbReplicationIds = []
      this.deleteData.forEach(e => deleteDbReplicationIds.push(e.dbReplicationId))
      this.axios.delete('/api/drc/v2/config/dbReplications?dbReplicationIds=' + deleteDbReplicationIds).then(res => {
        if (res.data.status === 1) {
          this.$Message.error('删除失败!' + res.data.message)
        } else {
          this.$Message.success('删除成功!')
          this.getDbAppliers()
          this.initInfo.multiData = []
        }
      })
    }
  },
  created () {
    this.axios.get('/api/drc/v2/permission/meta/mhaReplication/modify').then((response) => {
      if (response.data.status === 403) {
        this.$router.push('/nopermission')
        return
      }
      this.initInfo = {
        srcMhaName: this.$route.query.srcMhaName,
        srcMhaId: this.$route.query.srcMhaId,
        dstMhaName: this.$route.query.dstMhaName,
        applierGroupId: this.$route.query.applierGroupId,
        srcDc: this.$route.query.srcDc,
        dstDc: this.$route.query.dstDc,
        order: this.$route.query.order
      }
      console.log('initInfo:')
      console.log(this.initInfo)
      this.getResources()
      this.getDbAppliers()
    })
  }
}
</script>

<style scoped>

</style>
