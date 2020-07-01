class TreeNode {
  constructor(data) {
    this.data = data;
    this.children = [];
  }

  addChild(treeNode) {
    this.children.push(treeNode);
  }
}
